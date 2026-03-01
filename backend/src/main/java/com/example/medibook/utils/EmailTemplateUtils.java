package com.example.medibook.utils;

import java.util.Map;

public class EmailTemplateUtils {

    public static String getProfessionalTemplate(String title, String greeting, String mainMessage, Map<String, String> details, String actionUrl, String actionText, String color) {
        StringBuilder detailsHtml = new StringBuilder();
        if (details != null && !details.isEmpty()) {
            detailsHtml.append("<div style='background:#f9fafb; padding:20px; border-radius:12px; margin:20px 0; border:1px solid #f3f4f6;'>");
            details.forEach((k, v) -> {
                detailsHtml.append("<p style='margin:8px 0; font-size:14px; color:#4b5563;'>")
                           .append("<strong>").append(k).append(":</strong> ")
                           .append("<span style='color:#111827;'>").append(v).append("</span>")
                           .append("</p>");
            });
            detailsHtml.append("</div>");
        }

        String buttonHtml = "";
        if (actionUrl != null && actionText != null) {
            buttonHtml = String.format(
                "<div style='text-align:center; margin-top:24px;'>" +
                "<a href='%s' style='background:%s; color:white; padding:14px 28px; text-decoration:none; border-radius:10px; font-weight:700; display:inline-block;'>%s</a>" +
                "</div>", actionUrl, color, actionText);
        }

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #1f2937; margin: 0; padding: 0; }
                </style>
            </head>
            <body>
                <div style="max-width: 600px; margin: 20px auto; border: 1px solid #e5e7eb; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);">
                    <div style="background: %s; padding: 30px; text-align: center; color: white;">
                        <h1 style="margin: 0; font-size: 24px; font-weight: 800; letter-spacing: -0.5px;">%s</h1>
                    </div>
                    <div style="padding: 32px; background: white;">
                        <p style="font-size: 16px; margin-top: 0;">Chào <strong>%s</strong>,</p>
                        <p style="font-size: 15px; color: #4b5563;">%s</p>
                        
                        %s
                        
                        %s
                        
                        <div style="margin-top: 32px; padding-top: 24px; border-top: 1px solid #f3f4f6; color: #6b7280; font-size: 13px;">
                            <p>Vui lòng đến đúng giờ để được phục vụ tốt nhất. Nếu bạn có bất kỳ câu hỏi nào, hãy liên hệ với chúng tôi qua hotline.</p>
                            <p style="margin-top: 16px;">Trân trọng,<br><strong>Đội ngũ MediBook</strong></p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, color, title, greeting, mainMessage, detailsHtml.toString(), buttonHtml, color, title, greeting, mainMessage);
    }
}
