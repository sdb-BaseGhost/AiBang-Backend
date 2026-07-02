package org.sdb.aiban.common.util;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.List;

/**
 * Markdown -> HTML 转换工具
 */
public class MarkdownUtil {

    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;

    static {
        List<Extension> extensions = List.of(TablesExtension.create());
        PARSER = Parser.builder().extensions(extensions).build();
        RENDERER = HtmlRenderer.builder().extensions(extensions).build();
    }

    public static String toHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) return "";
        Node document = PARSER.parse(markdown);
        return RENDERER.render(document);
    }

    /**
     * 生成带基本样式的完整 HTML 页面（用于前端 RichText 渲染）
     */
    public static String toStyledHtml(String markdown) {
        String bodyHtml = toHtml(markdown);
        return "<!DOCTYPE html><html><head><meta charset=\"utf-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\">" +
                "<style>" +
                "body{font-family:-apple-system,sans-serif;font-size:15px;line-height:1.7;color:#1e293b;padding:4px 0;margin:0;word-wrap:break-word;}" +
                "p{margin:0 0 10px;}" +
                "strong{font-weight:600;}" +
                "ul,ol{padding-left:20px;margin:6px 0;}" +
                "li{margin:3px 0;}" +
                "code{background:#f1f5f9;padding:2px 6px;border-radius:4px;font-size:13px;font-family:monospace;}" +
                "pre{background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:12px;overflow-x:auto;margin:8px 0;}" +
                "pre code{background:none;padding:0;}" +
                "blockquote{border-left:3px solid #6366f1;padding-left:12px;margin:8px 0;color:#64748b;}" +
                "h1,h2,h3{font-weight:600;margin:12px 0 6px;}" +
                "h1{font-size:20px;}h2{font-size:18px;}h3{font-size:16px;}" +
                "table{border-collapse:collapse;width:100%;margin:8px 0;}" +
                "th,td{border:1px solid #e2e8f0;padding:6px 10px;text-align:left;font-size:14px;}" +
                "th{background:#f8fafc;font-weight:600;}" +
                "hr{border:none;border-top:1px solid #e2e8f0;margin:12px 0;}" +
                "a{color:#6366f1;}" +
                "</style></head><body>" + bodyHtml + "</body></html>";
    }
}
