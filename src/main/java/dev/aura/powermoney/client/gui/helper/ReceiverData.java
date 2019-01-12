package dev.aura.powermoney.client.gui.helper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import net.minecraft.client.resources.I18n;

@SuppressFBWarnings(
  value = {"JLM_JSR166_UTILCONCURRENT_MONITORENTER", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"},
  justification = "Code is generated by lombok which means I don't have any influence on it."
)
@Value
@Getter(AccessLevel.NONE)
public class ReceiverData {
  @Getter private final boolean waiting;
  @Getter private final boolean enabled;
  private final BigInteger localEnergyPerSecond;
  private final BigInteger totalEnergyPerSecond;
  private final BigDecimal moneyPerSecond;
  private final String moneySymbol;
  private final int defaultDigits;

  @Getter(value = AccessLevel.PRIVATE, lazy = true)
  private final DecimalFormatSymbols formatSymbols = generateFormatSymbols();

  @Getter(value = AccessLevel.PRIVATE, lazy = true)
  private final DecimalFormat intFormat = generateIntFormat();

  @Getter(value = AccessLevel.PRIVATE, lazy = true)
  private final DecimalFormat decimalFormat = generateDecimalFormat();

  @Getter(lazy = true)
  private final String localEnergyFormatted = generateEnergyFormatted(localEnergyPerSecond);

  @Getter(lazy = true)
  private final String totalEnergyFormatted = generateEnergyFormatted(totalEnergyPerSecond);

  @Getter(lazy = true)
  private final String moneyFormatted = generateMoneyFormatted();

  public static ReceiverData waiting() {
    return new ReceiverData(true);
  }

  public static ReceiverData receiverDisabled() {
    return new ReceiverData(false);
  }

  public static ReceiverData setReceiverData(
      BigInteger localEnergy,
      BigInteger totalEnergy,
      BigDecimal money,
      String moneySymbol,
      int defaultDigits) {
    return new ReceiverData(localEnergy, totalEnergy, money, moneySymbol, defaultDigits);
  }

  private ReceiverData(boolean waiting) {
    this.waiting = waiting;
    enabled = false;
    localEnergyPerSecond = null;
    totalEnergyPerSecond = null;
    moneyPerSecond = null;
    moneySymbol = null;
    defaultDigits = 0;
  }

  private ReceiverData(
      BigInteger localEnergy,
      BigInteger totalEnergy,
      BigDecimal money,
      String moneySymbol,
      int defaultDigits) {
    waiting = false;
    enabled = true;
    localEnergyPerSecond = localEnergy;
    totalEnergyPerSecond = totalEnergy;
    moneyPerSecond = money;
    this.moneySymbol = moneySymbol;
    this.defaultDigits = defaultDigits;
  }

  private DecimalFormatSymbols generateFormatSymbols() {
    final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
    formatSymbols.setDecimalSeparator(I18n.format("gui.powermoney.decimalseparator").charAt(0));
    formatSymbols.setGroupingSeparator(I18n.format("gui.powermoney.groupingseparator").charAt(0));

    return formatSymbols;
  }

  private DecimalFormat generateIntFormat() {
    final DecimalFormat intFormat = new DecimalFormat();
    intFormat.setMaximumFractionDigits(0);
    intFormat.setMinimumFractionDigits(0);
    intFormat.setGroupingUsed(true);
    intFormat.setDecimalFormatSymbols(getFormatSymbols());

    return intFormat;
  }

  private DecimalFormat generateDecimalFormat() {
    final DecimalFormat decimalFormat = new DecimalFormat();
    decimalFormat.setMaximumFractionDigits(defaultDigits);
    decimalFormat.setMinimumFractionDigits(defaultDigits);
    decimalFormat.setGroupingUsed(true);
    decimalFormat.setDecimalFormatSymbols(getFormatSymbols());

    return decimalFormat;
  }

  private String generateEnergyFormatted(BigInteger energy) {
    return getIntFormat().format(energy)
        + ' '
        + I18n.format("gui.powermoney.energyunit")
        + I18n.format("gui.powermoney.persecond");
  }

  private String generateMoneyFormatted() {
    return getDecimalFormat().format(moneyPerSecond)
        + ' '
        + moneySymbol
        + I18n.format("gui.powermoney.persecond");
  }
}
