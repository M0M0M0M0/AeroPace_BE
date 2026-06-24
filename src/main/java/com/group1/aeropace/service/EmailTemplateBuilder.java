package com.group1.aeropace.service;

import com.group1.aeropace.entity.Order;
import com.group1.aeropace.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class EmailTemplateBuilder {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String buildOrderConfirmation(Order order) {
        List<OrderItem> items = order.getOrderItems();

        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                  <title>Xác nhận đơn hàng</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f4;padding:32px 0;">
                    <tr><td align="center">
                      <table width="620" cellpadding="0" cellspacing="0"
                             style="background:#ffffff;border-radius:8px;overflow:hidden;
                                    box-shadow:0 2px 8px rgba(0,0,0,0.08);">

                        <!-- HEADER -->
                        <tr>
                          <td style="background:#1a1a2e;padding:28px 40px;text-align:center;">
                            <h1 style="margin:0;color:#ffffff;font-size:26px;letter-spacing:2px;">
                              AeroPace
                            </h1>
                            <p style="margin:6px 0 0;color:#a0a8c0;font-size:13px;">
                              Xác nhận đơn hàng
                            </p>
                          </td>
                        </tr>

                        <!-- GREETING -->
                        <tr>
                          <td style="padding:32px 40px 0;">
                            <p style="margin:0;font-size:15px;color:#333;">
                              Xin chào <strong>%s</strong>,
                            </p>
                            <p style="margin:10px 0 0;font-size:14px;color:#555;line-height:1.6;">
                              Cảm ơn bạn đã đặt hàng tại <strong>AeroPace</strong>.
                              Đơn hàng của bạn đã được ghi nhận và đang được xử lý.
                            </p>
                          </td>
                        </tr>

                        <!-- ORDER INFO -->
                        <tr>
                          <td style="padding:24px 40px 0;">
                            <table width="100%%" cellpadding="10" cellspacing="0"
                                   style="background:#f8f9fc;border-radius:6px;font-size:14px;">
                              <tr>
                                <td style="color:#777;width:40%%;">Mã đơn hàng</td>
                                <td style="color:#1a1a2e;font-weight:bold;">%s</td>
                              </tr>
                              <tr>
                                <td style="color:#777;">Ngày đặt hàng</td>
                                <td style="color:#333;">%s</td>
                              </tr>
                              <tr>
                                <td style="color:#777;">Trạng thái</td>
                                <td>
                                  <span style="background:#89fc62;
                                               padding:3px 10px;border-radius:12px;font-size:12px;">
                                    %s
                                  </span>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>

                        <!-- PRODUCTS TABLE -->
                        <tr>
                          <td style="padding:24px 40px 0;">
                            <p style="margin:0 0 10px;font-size:15px;font-weight:bold;color:#1a1a2e;">
                              Danh sách sản phẩm
                            </p>
                            <table width="100%%" cellpadding="0" cellspacing="0"
                                   style="border-collapse:collapse;font-size:13px;">
                              <thead>
                                <tr style="background:#1a1a2e;color:#fff;">
                                  <th style="padding:10px 12px;text-align:left;border-radius:4px 0 0 0;">
                                    Sản phẩm
                                  </th>
                                  <th style="padding:10px 12px;text-align:left;">Variant</th>
                                  <th style="padding:10px 12px;text-align:center;">SL</th>
                                  <th style="padding:10px 12px;text-align:right;">Đơn giá</th>
                                  <th style="padding:10px 12px;text-align:right;border-radius:0 4px 0 0;">
                                    Thành tiền
                                  </th>
                                </tr>
                              </thead>
                              <tbody>
                                %s
                              </tbody>
                            </table>
                          </td>
                        </tr>

                        <!-- PAYMENT SUMMARY -->
                        <tr>
                          <td style="padding:20px 40px 0;">
                            <table width="100%%" cellpadding="6" cellspacing="0"
                                   style="font-size:14px;border-top:1px solid #eee;">
                              <tr>
                                <td style="color:#777;">Tạm tính</td>
                                <td style="text-align:right;color:#333;">$%s</td>
                              </tr>
                              <tr>
                                <td style="color:#777;">Phí vận chuyển</td>
                                <td style="text-align:right;color:#333;">$%s</td>
                              </tr>
                              <tr>
                                <td style="color:#777;">VAT</td>
                                <td style="text-align:right;color:#333;">$%s</td>
                              </tr>
                              <tr style="border-top:2px solid #1a1a2e;">
                                <td style="font-weight:bold;color:#1a1a2e;padding-top:10px;">
                                  Tổng thanh toán
                                </td>
                                <td style="text-align:right;font-weight:bold;
                                           font-size:16px;padding-top:10px;">
                                  $%s
                                </td>
                              </tr>
                              <tr>
                                <td style="color:#777;padding-top:6px;">Phương thức thanh toán</td>
                                <td style="text-align:right;color:#333;padding-top:6px;">%s</td>
                              </tr>
                            </table>
                          </td>
                        </tr>

                        <!-- SHIPPING INFO -->
                        <tr>
                          <td style="padding:24px 40px 0;">
                            <p style="margin:0 0 10px;font-size:15px;font-weight:bold;color:#1a1a2e;">
                              Thông tin giao hàng
                            </p>
                            <table width="100%%" cellpadding="6" cellspacing="0"
                                   style="background:#f8f9fc;border-radius:6px;font-size:14px;">
                              <tr>
                                <td style="color:#777;width:40%%;">Người nhận</td>
                                <td style="color:#333;">%s</td>
                              </tr>
                              <tr>
                                <td style="color:#777;">Số điện thoại</td>
                                <td style="color:#333;">%s</td>
                              </tr>
                              <tr>
                                <td style="color:#777;">Địa chỉ</td>
                                <td style="color:#333;">%s</td>
                              </tr>
                            </table>
                          </td>
                        </tr>

                        <!-- FOOTER -->
                        <tr>
                          <td style="padding:32px 40px;text-align:center;border-top:1px solid #eee;margin-top:24px;">
                            <p style="margin:0;font-size:14px;color:#555;">
                              Cảm ơn bạn đã mua sắm tại <strong>AeroPace</strong>.
                            </p>
                            <p style="margin:8px 0 0;font-size:12px;color:#999;">
                              Nếu có thắc mắc, vui lòng liên hệ bộ phận hỗ trợ của chúng tôi.
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                // greeting
                order.getReceiverName(),
                // order info
                order.getOrderCode(),
                order.getCreatedAt().format(DATE_FMT),
                translateStatus(order.getStatus().name()),
                // product rows
                buildItemRows(items),
                // payment summary
                formatVnd(order.getTotalPrice()),
                formatVnd(order.getShippingFee()),
                formatVnd(order.getVat()),
                formatVnd(calculateGrandTotal(order)),
                order.getPaymentMethod().name(),
                // shipping
                order.getReceiverName(),
                order.getPhoneNumber(),
                buildFullAddress(order)
        );
    }

    public String buildRefundConfirmation(Order order) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                  <title>Xác nhận hoàn tiền</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f4;padding:32px 0;">
                    <tr><td align="center">
                      <table width="620" cellpadding="0" cellspacing="0"
                             style="background:#ffffff;border-radius:8px;overflow:hidden;
                                    box-shadow:0 2px 8px rgba(0,0,0,0.08);">

                        <!-- HEADER -->
                        <tr>
                          <td style="background:#1a1a2e;padding:28px 40px;text-align:center;">
                            <h1 style="margin:0;color:#ffffff;font-size:26px;letter-spacing:2px;">
                              AeroPace
                            </h1>
                            <p style="margin:6px 0 0;color:#a0a8c0;font-size:13px;">
                              Xác nhận hoàn tiền
                            </p>
                          </td>
                        </tr>

                        <!-- GREETING -->
                        <tr>
                          <td style="padding:32px 40px 0;">
                            <p style="margin:0;font-size:15px;color:#333;">
                              Xin chào <strong>%s</strong>,
                            </p>
                            <p style="margin:10px 0 0;font-size:14px;color:#555;line-height:1.6;">
                              Yêu cầu hoàn tiền cho đơn hàng của bạn tại <strong>AeroPace</strong>
                              đã được xử lý thành công.
                            </p>
                          </td>
                        </tr>

                        <!-- REFUND INFO -->
                        <tr>
                          <td style="padding:24px 40px 0;">
                            <table width="100%%" cellpadding="10" cellspacing="0"
                                   style="background:#f8f9fc;border-radius:6px;font-size:14px;">
                              <tr>
                                <td style="color:#777;width:40%%;">Mã đơn hàng</td>
                                <td style="color:#1a1a2e;font-weight:bold;">%s</td>
                              </tr>
                              <tr>
                                <td style="color:#777;">Số tiền hoàn</td>
                                <td style="color:#333;font-weight:bold;">$%s</td>
                              </tr>
                              <tr>
                                <td style="color:#777;">Phương thức thanh toán</td>
                                <td style="color:#333;">%s</td>
                              </tr>
                              <tr>
                                <td style="color:#777;">Lý do hoàn tiền</td>
                                <td style="color:#333;">%s</td>
                              </tr>
                              <tr>
                                <td style="color:#777;">Trạng thái</td>
                                <td>
                                  <span style="background:#89fc62;
                                               padding:3px 10px;border-radius:12px;font-size:12px;">
                                    Đã hoàn tiền
                                  </span>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>

                        <!-- FOOTER -->
                        <tr>
                          <td style="padding:32px 40px;text-align:center;border-top:1px solid #eee;margin-top:24px;">
                            <p style="margin:0;font-size:14px;color:#555;">
                              Số tiền sẽ được hoàn về phương thức thanh toán ban đầu của bạn
                              trong vài ngày làm việc tới.
                            </p>
                            <p style="margin:8px 0 0;font-size:12px;color:#999;">
                              Nếu có thắc mắc, vui lòng liên hệ bộ phận hỗ trợ của chúng tôi.
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                order.getReceiverName(),
                order.getOrderCode(),
                formatVnd(calculateGrandTotal(order)),
                order.getPaymentMethod().name(),
                order.getRefundReason() != null && !order.getRefundReason().isBlank()
                        ? order.getRefundReason() : "Không có"
        );
    }

    private String buildItemRows(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            String bg = i % 2 == 0 ? "#ffffff" : "#f8f9fc";
            BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            sb.append("""
                    <tr style="background:%s;">
                      <td style="padding:10px 12px;color:#333;border-bottom:1px solid #eee;">%s</td>
                      <td style="padding:10px 12px;color:#555;border-bottom:1px solid #eee;">%s</td>
                      <td style="padding:10px 12px;text-align:center;color:#333;border-bottom:1px solid #eee;">%d</td>
                      <td style="padding:10px 12px;text-align:right;color:#333;border-bottom:1px solid #eee;">$%s</td>
                      <td style="padding:10px 12px;text-align:right;font-weight:bold;color:#1a1a2e;border-bottom:1px solid #eee;">$%s</td>
                    </tr>
                    """.formatted(
                    bg,
                    item.getProductName(),
                    item.getVariantName() != null ? item.getVariantName() : "",
                    item.getQuantity(),
                    formatVnd(item.getPrice()),
                    formatVnd(lineTotal)
            ));
        }
        return sb.toString();
    }

    private BigDecimal calculateGrandTotal(Order order) {
        BigDecimal total = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal shipping = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        BigDecimal vat = order.getVat() != null ? order.getVat() : BigDecimal.ZERO;
        return total.add(shipping).add(vat);
    }

    private String buildFullAddress(Order order) {
        return List.of(
                        order.getShippingAddress() != null ? order.getShippingAddress() : "",
                        order.getWard() != null ? order.getWard() : "",
                        order.getDistrict() != null ? order.getDistrict() : "",
                        order.getProvince() != null ? order.getProvince() : ""
                ).stream()
                .filter(s -> !s.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private String formatVnd(BigDecimal amount) {
        if (amount == null) return "0.00";
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(amount);
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "PENDING"   -> "Chờ thanh toán";
            case "PAID"      -> "Đã thanh toán";
            case "SHIPPING"  -> "Đang giao hàng";
            case "DELIVERED" -> "Đã giao hàng";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            default          -> status;
        };
    }
}