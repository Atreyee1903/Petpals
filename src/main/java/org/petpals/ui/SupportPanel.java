package org.petpals.ui;

import org.petpals.db.QueryDAO;
import org.petpals.model.Query;
import org.petpals.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

public class SupportPanel extends JPanel {

    private final User currentUser;
    private final QueryDAO queryDAO;

    private JTextArea queryInputArea;
    private JButton submitQueryButton;
    private JTable queriesTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScrollPane;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public SupportPanel(User user) {
        this.currentUser = user;
        this.queryDAO = new QueryDAO();
        setLayout(new BorderLayout(10, 10)); // Add gaps between components
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        if (currentUser.isAdmin()) {
            setupAdminView();
        } else {
            setupUserView();
        }
        refreshQueriesTable(); // Load initial data
    }

    private void setupUserView() {
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("Submit a New Query"));

        queryInputArea = new JTextArea(5, 30);
        queryInputArea.setLineWrap(true);
        queryInputArea.setWrapStyleWord(true);
        JScrollPane inputScrollPane = new JScrollPane(queryInputArea);
        topPanel.add(inputScrollPane, BorderLayout.CENTER);

        submitQueryButton = new JButton("Submit Query");
        submitQueryButton.addActionListener(this::submitUserQuery);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(submitQueryButton);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Table to display user's past queries
        setupQueriesTable(false); // is Admin = false
        add(tableScrollPane, BorderLayout.CENTER);

        // Add delete button for user view
        addDeleteButton();
    }

    private void setupAdminView() {
        setBorder(BorderFactory.createTitledBorder("Manage Support Queries"));
        // Table to display all open queries for admin
        setupQueriesTable(true); // is Admin = true
        add(tableScrollPane, BorderLayout.CENTER);

        // Add buttons for replying and deleting
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton replyButton = new JButton("Reply to Selected Query");
        replyButton.addActionListener(this::showReplyDialog);
        bottomPanel.add(replyButton);

        JButton deleteButton = new JButton("Delete Selected Query");
        deleteButton.setForeground(Constants.COLOR_ERROR); // Make delete button red
        deleteButton.addActionListener(this::deleteSelectedQuery);
        bottomPanel.add(deleteButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Helper method to add delete button (used in both views)
    private void addDeleteButton() {
         // Create a new panel specifically for the delete button(s)
         JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

         JButton deleteButton = new JButton("Delete Selected Query");
         deleteButton.setForeground(Constants.COLOR_ERROR); // Make delete button red
         deleteButton.addActionListener(this::deleteSelectedQuery);
         bottomButtonPanel.add(deleteButton); // Add button to the new panel

         // Add this new panel to the SOUTH region.
         // If called from admin view, this replaces the previous bottom panel.
         // If called from user view, this adds the bottom panel.
         add(bottomButtonPanel, BorderLayout.SOUTH);
    }

    private void setupQueriesTable(boolean isAdmin) {
        Vector<String> columnNames = new Vector<>();
        if (isAdmin) {
            columnNames.add("User"); // Show username for admin
        }
        columnNames.add("Query");
        columnNames.add("Submitted");
        columnNames.add("Status");
        columnNames.add("Reply");
        columnNames.add("Replied");
        columnNames.add("Query ID"); // Hidden column to store ID

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        queriesTable = new JTable(tableModel);
        queriesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        queriesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Allow horizontal scrolling

        // Hide the Query ID column
        TableColumn idColumn = queriesTable.getColumnModel().getColumn(queriesTable.getColumnCount() - 1);
        idColumn.setMinWidth(0);
        idColumn.setMaxWidth(0);
        idColumn.setWidth(0);
        idColumn.setPreferredWidth(0);

        // Set preferred widths for other columns
        setColumnWidths(isAdmin);

        // Use a custom renderer for potentially long text fields (Query, Reply)
        TableCellRenderer textRenderer = new TextAreaRenderer();
        queriesTable.getColumnModel().getColumn(isAdmin ? 1 : 0).setCellRenderer(textRenderer); // Query column
        queriesTable.getColumnModel().getColumn(isAdmin ? 4 : 3).setCellRenderer(textRenderer); // Reply column

        queriesTable.setFillsViewportHeight(true); // Table uses entire height of scroll pane
        tableScrollPane = new JScrollPane(queriesTable);
    }

    private void setColumnWidths(boolean isAdmin) {
        TableColumn column;
        int userCol = isAdmin ? 0 : -1;
        int queryCol = isAdmin ? 1 : 0;
        int submittedCol = isAdmin ? 2 : 1;
        int statusCol = isAdmin ? 3 : 2;
        int replyCol = isAdmin ? 4 : 3;
        int repliedCol = isAdmin ? 5 : 4;

        if (isAdmin) {
            column = queriesTable.getColumnModel().getColumn(userCol);
            column.setPreferredWidth(100);
        }
        column = queriesTable.getColumnModel().getColumn(queryCol);
        column.setPreferredWidth(300); // Query text can be long
        column = queriesTable.getColumnModel().getColumn(submittedCol);
        column.setPreferredWidth(120);
        column = queriesTable.getColumnModel().getColumn(statusCol);
        column.setPreferredWidth(80);
        column = queriesTable.getColumnModel().getColumn(replyCol);
        column.setPreferredWidth(300); // Reply text can be long
        column = queriesTable.getColumnModel().getColumn(repliedCol);
        column.setPreferredWidth(120);
    }


    private void refreshQueriesTable() {
        tableModel.setRowCount(0); // Clear existing data
        List<Query> queries;
        boolean isAdmin = currentUser.isAdmin();

        if (isAdmin) {
            queries = queryDAO.getAllQueries(); // Show all for admin for now, maybe filter later
        } else {
            queries = queryDAO.getQueriesByUserId(currentUser.getId());
        }

        for (Query query : queries) {
            Vector<Object> row = new Vector<>();
            if (isAdmin) {
                row.add(query.getUsername()); // Add username for admin view
            }
            row.add(query.getQueryText());
            row.add(formatTimestamp(query.getQueryTimestamp()));
            row.add(query.getStatus());
            row.add(query.getAdminReply() != null ? query.getAdminReply() : "");
            row.add(formatTimestamp(query.getReplyTimestamp()));
            row.add(query.getQueryId()); // Add hidden ID
            tableModel.addRow(row);
        }
         // Adjust row heights after adding data
        adjustRowHeights();
    }

     private void adjustRowHeights() {
        for (int row = 0; row < queriesTable.getRowCount(); row++) {
            int rowHeight = queriesTable.getRowHeight();
            for (int column = 0; column < queriesTable.getColumnCount(); column++) {
                Component comp = queriesTable.prepareRenderer(queriesTable.getCellRenderer(row, column), row, column);
                // Only consider visible columns for height calculation
                 if (queriesTable.getColumnModel().getColumn(column).getWidth() > 0) {
                    rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
                 }
            }
            queriesTable.setRowHeight(row, rowHeight);
        }
    }

    private String formatTimestamp(Timestamp timestamp) {
        return timestamp != null ? DATE_FORMAT.format(timestamp) : "";
    }

    private void submitUserQuery(ActionEvent e) {
        String queryText = queryInputArea.getText().trim();
        if (queryText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Query cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Query newQuery = new Query();
        newQuery.setUserId(currentUser.getId());
        newQuery.setQueryText(queryText);

        boolean success = queryDAO.addQuery(newQuery);
        if (success) {
            JOptionPane.showMessageDialog(this, "Query submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            queryInputArea.setText(""); // Clear input area
            refreshQueriesTable(); // Refresh the table
        } else {
            JOptionPane.showMessageDialog(this, "Failed to submit query. Please try again.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showReplyDialog(ActionEvent e) {
        int selectedRow = queriesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a query to reply to.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get query details from the hidden ID column
        int queryIdColumnIndex = queriesTable.getColumnCount() - 1;
        int queryId = (int) tableModel.getValueAt(selectedRow, queryIdColumnIndex);
        String currentQueryText = (String) tableModel.getValueAt(selectedRow, 1); // Query text column
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 3); // Status column

        if (!currentStatus.equalsIgnoreCase("Open")) {
             JOptionPane.showMessageDialog(this, "This query has already been answered or is closed.", "Info", JOptionPane.INFORMATION_MESSAGE);
             return;
        }


        // Create a dialog for the reply
        JDialog replyDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reply to Query", true);
        replyDialog.setLayout(new BorderLayout(10, 10));
        replyDialog.setSize(450, 350);
        replyDialog.setLocationRelativeTo(this);

        JTextArea queryDisplayArea = new JTextArea("Query: \n" + currentQueryText);
        queryDisplayArea.setEditable(false);
        queryDisplayArea.setLineWrap(true);
        queryDisplayArea.setWrapStyleWord(true);
        queryDisplayArea.setBackground(this.getBackground()); // Match panel background
        JScrollPane queryScrollPane = new JScrollPane(queryDisplayArea);
        queryScrollPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));


        JTextArea replyInputArea = new JTextArea(5, 30);
        replyInputArea.setLineWrap(true);
        replyInputArea.setWrapStyleWord(true);
        JScrollPane replyScrollPane = new JScrollPane(replyInputArea);
        replyScrollPane.setBorder(BorderFactory.createTitledBorder("Admin Reply"));

        JButton submitReplyButton = new JButton("Submit Reply");
        submitReplyButton.addActionListener(event -> {
            String replyText = replyInputArea.getText().trim();
            if (replyText.isEmpty()) {
                JOptionPane.showMessageDialog(replyDialog, "Reply cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean success = queryDAO.updateQueryReply(queryId, replyText);
            if (success) {
                JOptionPane.showMessageDialog(replyDialog, "Reply submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                replyDialog.dispose();
                refreshQueriesTable(); // Refresh the main table
            } else {
                JOptionPane.showMessageDialog(replyDialog, "Failed to submit reply.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(submitReplyButton);

        replyDialog.add(queryScrollPane, BorderLayout.NORTH);
        replyDialog.add(replyScrollPane, BorderLayout.CENTER);
        replyDialog.add(buttonPanel, BorderLayout.SOUTH);

        replyDialog.setVisible(true);
    }

    private void deleteSelectedQuery(ActionEvent e) {
        int selectedRow = queriesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a query to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete this query?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Get query ID from the hidden column
            int queryIdColumnIndex = queriesTable.getColumnCount() - 1;
            int queryId = (int) tableModel.getValueAt(selectedRow, queryIdColumnIndex);

            boolean success = queryDAO.deleteQuery(queryId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Query deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshQueriesTable(); // Refresh the table
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete query.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

     // Custom renderer to wrap text in table cells
    static class TextAreaRenderer extends JTextArea implements TableCellRenderer {
        public TextAreaRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2)); // Add padding
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
            if (table.getRowHeight(row) < getPreferredSize().height) {
                 // Only adjust if needed, prevents infinite loop with adjustRowHeights
                 // table.setRowHeight(row, getPreferredSize().height); // Let adjustRowHeights handle this
            }

             if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            return this;
        }
    }
}
