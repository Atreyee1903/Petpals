package org.petpals.ui;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

// Central place for UI constants like colors and fonts
public class Constants {

  // --- Colors ---
  public static final Color COLOR_BACKGROUND = new Color(248, 247, 243); // Very light warm off-white
  public static final Color COLOR_PANEL_BACKGROUND = Color.WHITE;
  public static final Color COLOR_PRIMARY = new Color(70, 130, 180);    // Steel Blue (Friendly but clear)
  public static final Color COLOR_PRIMARY_LIGHT = new Color(135, 206, 250); // Light Sky Blue for accents maybe
  public static final Color COLOR_ACCENT = new Color(230, 140, 70);     // Soft Orange/Terracotta
  public static final Color COLOR_TEXT_PRIMARY = new Color(40, 40, 40);     // Dark Grey
  public static final Color COLOR_TEXT_SECONDARY = new Color(100, 100, 100);  // Medium Grey
  public static final Color COLOR_BORDER = new Color(210, 210, 210);     // Light Grey Border
  public static final Color COLOR_PRICE = new Color(0, 110, 50);       // Dark Green for Price
  public static final Color COLOR_SUCCESS = new Color(60, 179, 113);    // Medium Sea Green
  public static final Color COLOR_ERROR = new Color(220, 53, 69);      // Red for errors


  // --- Fonts ---
  // Using default Look & Feel fonts but defining styles
  public static final Font FONT_HEADING_1 = UIManager.getFont("h1.font"); // Use Look & Feel sizes if defined
  public static final Font FONT_HEADING_2 = UIManager.getFont("h2.font");
  public static final Font FONT_HEADING_3 = UIManager.getFont("h3.font");
  public static final Font FONT_NORMAL = UIManager.getFont("Label.font");
  public static final Font FONT_BUTTON = UIManager.getFont("Button.font");
  public static final Font FONT_ITALIC = FONT_NORMAL.deriveFont(Font.ITALIC);
  public static final Font FONT_BOLD = FONT_NORMAL.deriveFont(Font.BOLD);
  // --- Dimensions ---
  public static final int CARD_H_GAP = 15;
  public static final int CARD_V_GAP = 15;
  public static final int DIALOG_PADDING = 15;
  public static final int PANEL_PADDING = 10;
  // --- Formatting ---
  private static final Locale INDIA_LOCALE = new Locale("en", "IN");
  public static final NumberFormat INR_CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(INDIA_LOCALE);

}