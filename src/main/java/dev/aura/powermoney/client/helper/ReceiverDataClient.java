package dev.aura.powermoney.client.helper;

import dev.aura.powermoney.common.helper.ReceiverData;
import dev.aura.powermoney.common.tileentity.TileEntityPowerReceiver;
import dev.aura.powermoney.network.PacketDispatcher;
import dev.aura.powermoney.network.packet.serverbound.PacketChangeRequiresReceiverData;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;

@SuppressFBWarnings(
  value = {"JLM_JSR166_UTILCONCURRENT_MONITORENTER", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"},
  justification = "Code is generated by lombok which means I don't have any influence on it."
)
@Data
@Setter(AccessLevel.NONE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ReceiverDataClient extends ReceiverData {
  @Getter private static ReceiverDataClient instance;

  static {
    // Start off disabled
    receiverDisabled();
  }

  public static ReceiverDataClient createWaiting() {
    return new ReceiverDataClient(true);
  }

  public static void waiting() {
    instance = createWaiting();
  }

  public static void waiting(TileEntityPowerReceiver tileEntity) {
    waiting();

    if (tileEntity != null)
      PacketDispatcher.sendToServer(
          PacketChangeRequiresReceiverData.startData(tileEntity.getOwner(), tileEntity.getPos()));
  }

  public static ReceiverDataClient createReceiverDisabled() {
    return new ReceiverDataClient(false);
  }

  public static void receiverDisabled() {
    instance = createReceiverDisabled();
  }

  public static ReceiverDataClient createReceiverData(
      long localEnergy, long totalEnergy, BigDecimal money, String moneySymbol, int defaultDigits) {
    return new ReceiverDataClient(localEnergy, totalEnergy, money, moneySymbol, defaultDigits);
  }

  public static void setReceiverData(
      long localEnergy, long totalEnergy, BigDecimal money, String moneySymbol, int defaultDigits) {
    instance = createReceiverData(localEnergy, totalEnergy, money, moneySymbol, defaultDigits);
  }

  public static void stopSending() {
    PacketDispatcher.sendToServer(PacketChangeRequiresReceiverData.stopData());
  }

  public String formatOwnerName(TileEntityPowerReceiver tileEntity, EntityPlayer player) {
    return formatOwnerName(tileEntity, player.getUniqueID());
  }

  public String formatOwnerName(TileEntityPowerReceiver tileEntity, UUID player) {
    final String ownerName = tileEntity.getOwnerName();

    if (player.equals(tileEntity.getOwner())) {
      return ownerName;
    } else {
      return ownerName + getNotYouMessage();
    }
  }

  protected ReceiverDataClient(boolean waiting) {
    super(waiting);
  }

  protected ReceiverDataClient(
      long localEnergy, long totalEnergy, BigDecimal money, String moneySymbol, int defaultDigits) {
    super(localEnergy, totalEnergy, money, moneySymbol, defaultDigits);
  }

  protected DecimalFormatSymbols generateFormatSymbols() {
    final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
    formatSymbols.setDecimalSeparator(I18n.format("gui.powermoney.decimalseparator").charAt(0));
    formatSymbols.setGroupingSeparator(I18n.format("gui.powermoney.groupingseparator").charAt(0));

    return formatSymbols;
  }

  protected DecimalFormat generateIntFormat() {
    final DecimalFormat intFormat = new DecimalFormat();
    intFormat.setMaximumFractionDigits(0);
    intFormat.setMinimumFractionDigits(0);
    intFormat.setGroupingUsed(true);
    intFormat.setDecimalFormatSymbols(getFormatSymbols());

    return intFormat;
  }

  protected DecimalFormat generateDecimalFormat() {
    final DecimalFormat decimalFormat = new DecimalFormat();
    decimalFormat.setMaximumFractionDigits(defaultDigits);
    decimalFormat.setMinimumFractionDigits(defaultDigits);
    decimalFormat.setGroupingUsed(true);
    decimalFormat.setDecimalFormatSymbols(getFormatSymbols());

    return decimalFormat;
  }

  protected String generateEnergyFormatted(long energy) {
    return getIntFormat().format(energy)
        + ' '
        + I18n.format("gui.powermoney.energyunit")
        + I18n.format("gui.powermoney.persecond");
  }

  protected String generateMoneyFormatted() {
    return getDecimalFormat().format(moneyPerSecond)
        + ' '
        + moneySymbol
        + I18n.format("gui.powermoney.persecond");
  }

  protected String[] generateHeadings() {
    return new String[] {
      I18n.format("gui.powermoney.owner"),
      I18n.format("gui.powermoney.localenergy"),
      I18n.format("gui.powermoney.totalenergy"),
      I18n.format("gui.powermoney.totalearning")
    };
  }

  protected String generateMessage() {
    return waiting ? I18n.format("gui.powermoney.waiting") : I18n.format("gui.powermoney.disabled");
  }

  protected String generateNotYouMessage() {
    return " (" + I18n.format("gui.powermoney.owner.notyou") + ')';
  }
}
