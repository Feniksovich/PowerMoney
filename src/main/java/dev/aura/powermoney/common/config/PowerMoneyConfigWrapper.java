package dev.aura.powermoney.common.config;

import dev.aura.powermoney.common.payment.MoneyCalculator;
import dev.aura.powermoney.common.payment.MoneyCalculatorLog;
import dev.aura.powermoney.common.payment.MoneyCalculatorRoot;
import java.util.List;
import lombok.Getter;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PowerMoneyConfigWrapper {
  public static final String CAT_CALCULATION = "calculation";
  public static final String CAT_CALCULATION_LOG = CAT_CALCULATION + ".logarithm";
  public static final String CAT_CALCULATION_ROOT = CAT_CALCULATION + ".root";
  public static final String CAT_PAYMENT = "payment";

  private static Configuration configStorage;

  @Getter private static double logBaseMultiplier;
  @Getter private static double logBase;
  @Getter private static double logShift;

  @Getter private static double rootBaseMultiplier;
  @Getter private static double rootBase;
  @Getter private static double rootShift;

  @Getter private static int calcType;
  @Getter private static MoneyCalculator moneyCalculator;

  @Getter private static String currency;
  @Getter private static String moneyInterface;
  @Getter private static int payoutInterval;
  @Getter private static boolean simulate;

  public static void loadConfig() {
    loadCalculationSettings();
    loadPaymentSettings();

    saveIfChanged();
  }

  public static void loadConfig(Configuration config) {
    configStorage = config;
    configStorage.load();

    loadConfig();
  }

  private static void loadCalculationSettings() {
    calcType =
        getInt(
            CAT_CALCULATION,
            "CalcType",
            0,
            0,
            1,
            "Choose the type of calculation.\n" + " - 0: logarithm\n" + " - 1: root");

    logBaseMultiplier =
        getDouble(
            CAT_CALCULATION_LOG,
            "LogBaseMultiplier",
            0.10,
            1E-6,
            1E6,
            "The base multiplier in the log calculation.\n"
                + "Essentially how much 1 unit of energy per second is worth.");
    logBase =
        getDouble(
            CAT_CALCULATION_LOG,
            "LogBase",
            2,
            Math.nextUp(1.0),
            1E6,
            "The logarithmic base in the calculation.\n"
                + "The higher the value the less money the players get.");
    logShift =
        getDouble(
            CAT_CALCULATION_LOG,
            "LogShift",
            0,
            -1E10,
            1E10,
            "The value that will be added each time to the final log calculation result.\n"
                + "Helps to adjust the energy price.");

    rootBaseMultiplier =
        getDouble(
            CAT_CALCULATION_ROOT,
            "RootBaseMultiplier",
            0.10,
            1E-6,
            1E6,
            "The base multiplier in the root calculation.\n"
                + "Essentially how much 1 unit of energy per second is worth.");
    rootBase =
        getDouble(
            CAT_CALCULATION_ROOT,
            "RootBase",
            2,
            Math.nextUp(0.0),
            1E6,
            "The root base in the calculation.\n"
                + "The higher the value the less money the players get.");
    rootShift =
        getDouble(
            CAT_CALCULATION_ROOT,
            "RootShift",
            0,
            -1E10,
            1E10,
            "The value that will be added each time to the final root calculation result.\n"
                + "Helps to adjust the energy price.");

    switch (calcType) {
      case (0):
        moneyCalculator = new MoneyCalculatorLog(logBaseMultiplier, logBase, logShift);
        break;
      case (1):
        moneyCalculator = new MoneyCalculatorRoot(rootBaseMultiplier, rootBase, rootShift);
        break;
      default:
        throw new IllegalArgumentException("Unknown calculation type.");
    }

    addCustomCategoryComment(
        CAT_CALCULATION,
        "Here you can tweak the calculations that converts energy into money.\n"
            + "\n"
            + "You can choose one of two formulas for calculating the energy price.\n"
            + "\n"
            + "Logarithmic formula:\n"
            + "    MoneyPerSecond = LogShift + LogBaseMultiplier * (log_LogBase(EnergyPerSecond) + 1)\n"
            + "\n"
            + "Root formula:\n"
            + "    MoneyPerSecond = RootShift + RootBaseMultiplier * root_RootBase(EnergyPerSecond)");
    addCustomCategoryComment(
        CAT_CALCULATION_LOG,
        "Logarithmic money calculation.\n"
            + "\n"
            + "The money is calcuated like this:\n"
            + "    MoneyPerSecond = LogShift + LogBaseMultiplier * (log_LogBase(EnergyPerSecond) + 1)");
    addCustomCategoryComment(
        CAT_CALCULATION_ROOT,
        "Root money calculation.\n"
            + "\n"
            + "The money is calcuated like this:\n"
            + "    MoneyPerSecond = RootShift + RootBaseMultiplier * root_RootBase(EnergyPerSecond)");
  }

  private static void loadPaymentSettings() {
    currency =
        getString(
            CAT_PAYMENT,
            "Currency",
            "",
            "The currency to make the payments.\n"
                + "If the currency specified here doesn't exist or is empty, then the system will fallback to the\n"
                + "default currency.\n"
                + "Only really needs to be set if there's more than one currency.");
    moneyInterface =
        getString(
            CAT_PAYMENT,
            "MoneyInterface",
            "auto",
            "Which MoneyInterface to use.\n"
                + "Check the debug log for available MoneyInterfaces. If the specified interface cannot be found or is\n"
                + "\"auto\", the system automatically select one. It will prioritize mod interfaces over the Sponge API.\n"
                + "Only really needs to be set if there's more than one MoneyInterface and default one is not the\n"
                + "one you want to use.");
    payoutInterval =
        getInt(
            CAT_PAYMENT,
            "PayoutInterval",
            15,
            1,
            1000,
            "The interval in seconds between payouts.\n"
                + "The value 1 means instant payouts (the money the player gets is calculated on a per second base).");
    simulate =
        getBoolean(
            CAT_PAYMENT,
            "Simulate",
            false,
            "If no MoneyInterface can be used, the blocks will not consume energy. Enabling this will make them\n"
                + "consume energy. But it will not produce any money. This is useful for testing and shouldn't be\n"
                + "enabled on a production server.\n"
                + "If any MoneyInterface is found, this setting has no effect!");

    addCustomCategoryComment(CAT_PAYMENT, "Settings regarding the payment to the players.");
  }

  private static String getDefaultLangKey(String category) {
    return "gui.powermoney.config.cat." + category.toLowerCase();
  }

  private static String getDefaultLangKey(String category, String name) {
    return getDefaultLangKey(category) + '.' + name.toLowerCase();
  }

  /**
   * Creates a boolean property.
   *
   * @param category Category of the property.
   * @param name Name of the property.
   * @param defaultValue Default value of the property.
   * @param comment A brief description what the property does.
   * @return The value of the new boolean property.
   */
  private static boolean getBoolean(
      String category, String name, boolean defaultValue, String comment) {
    Property prop = configStorage.get(category, name, defaultValue);
    prop.setLanguageKey(getDefaultLangKey(category, name));
    prop.setComment(comment + "\n[default: " + defaultValue + "]");

    return prop.getBoolean(defaultValue);
  }

  /**
   * Creates an integer property.
   *
   * @param category Category of the property.
   * @param name Name of the property.
   * @param defaultValue Default value of the property.
   * @param minValue Minimum value of the property.
   * @param maxValue Maximum value of the property.
   * @param comment A brief description what the property does.
   * @return The value of the new integer property.
   */
  private static int getInt(
      String category, String name, int defaultValue, int minValue, int maxValue, String comment) {
    Property prop = configStorage.get(category, name, defaultValue);
    prop.setLanguageKey(getDefaultLangKey(category, name));
    prop.setComment(
        comment + "\n[range: " + minValue + " ~ " + maxValue + ", default: " + defaultValue + "]");
    prop.setMinValue(minValue);
    prop.setMaxValue(maxValue);

    int readValue = prop.getInt(defaultValue);
    int cappedValue = Math.max(Math.min(readValue, maxValue), minValue);

    if (readValue != cappedValue) {
      prop.set(cappedValue);
    }

    return cappedValue;
  }

  /**
   * Creates a double property.
   *
   * @param category Category of the property.
   * @param name Name of the property.
   * @param defaultValue Default value of the property.
   * @param minValue Minimum value of the property.
   * @param maxValue Maximum value of the property.
   * @param comment A brief description what the property does.
   * @return The value of the new double property.
   */
  private static double getDouble(
      String category,
      String name,
      double defaultValue,
      double minValue,
      double maxValue,
      String comment) {
    Property prop = configStorage.get(category, name, defaultValue);
    prop.setLanguageKey(getDefaultLangKey(category, name));
    prop.setComment(
        comment + "\n[range: " + minValue + " ~ " + maxValue + ", default: " + defaultValue + "]");
    prop.setMinValue(minValue);
    prop.setMaxValue(maxValue);

    double readValue = prop.getDouble(defaultValue);
    double cappedValue = Math.max(Math.min(readValue, maxValue), minValue);

    if (readValue != cappedValue) {
      prop.set(cappedValue);
    }

    return cappedValue;
  }

  /**
   * Creates a String property.
   *
   * @param category Category of the property.
   * @param name Name of the property.
   * @param defaultValue Default value of the property.
   * @param comment A brief description what the property does.
   * @return The value of the new String property.
   */
  private static String getString(
      String category, String name, String defaultValue, String comment) {
    Property prop = configStorage.get(category, name, defaultValue);
    prop.setLanguageKey(getDefaultLangKey(category, name));
    prop.setComment(comment + "\n[default: " + defaultValue + "]");
    prop.setDefaultValue(defaultValue);

    return prop.getString();
  }

  /**
   * Adds a comment to the specified ConfigCategory object
   *
   * @param category the config category
   * @param comment a String comment
   */
  private static void addCustomCategoryComment(String category, String comment) {
    configStorage.setCategoryComment(category, comment);
    configStorage.setCategoryLanguageKey(category, getDefaultLangKey(category));
  }

  private static void saveIfChanged() {
    if (configStorage.hasChanged()) {
      configStorage.save();
    }
  }

  @SideOnly(Side.CLIENT)
  public static List<IConfigElement> getCalculationCategory() {
    return new ConfigElement(configStorage.getCategory(CAT_CALCULATION)).getChildElements();
  }

  @SideOnly(Side.CLIENT)
  public static List<IConfigElement> getPaymentCategory() {
    return new ConfigElement(configStorage.getCategory(CAT_PAYMENT)).getChildElements();
  }
}
