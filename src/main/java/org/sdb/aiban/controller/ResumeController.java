package org.sdb.aiban.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.sdb.aiban.common.result.PageResult;
import org.sdb.aiban.common.result.Result;
import org.sdb.aiban.dto.response.ResumeVO;
import org.sdb.aiban.service.ResumeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Tag(name = "简历管理", description = "简历上传、分析、优化")
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @Operation(summary = "上传简历（PDF文件）")
    @PostMapping("/upload")
    public Result<ResumeVO> uploadResume(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) throws IOException {
        Long userId = (Long) authentication.getPrincipal();

        // 提取PDF文本
        String content = extractTextFromPdf(file);

        // 使用文件名作为标题（如果未指定）
        String resumeTitle = (title != null && !title.isEmpty()) ? title : file.getOriginalFilename();

        return Result.success(resumeService.uploadResume(userId, resumeTitle, content));
    }

    @Data
    static class UploadBase64Request {
        private String fileName;
        private String fileContent; // Base64 encoded
        private String title;
    }

    @Operation(summary = "上传简历（Base64编码）")
    @PostMapping("/upload-base64")
    public Result<ResumeVO> uploadResumeBase64(
            Authentication authentication,
            @RequestBody UploadBase64Request request) throws IOException {
        Long userId = (Long) authentication.getPrincipal();

        // 解码 Base64
        byte[] pdfBytes = Base64.getDecoder().decode(request.getFileContent());

        // 提取PDF文本
        String content = extractTextFromPdfBytes(pdfBytes);

        // 使用文件名作为标题（如果未指定）
        String resumeTitle = (request.getTitle() != null && !request.getTitle().isEmpty())
                ? request.getTitle() : request.getFileName();

        return Result.success(resumeService.uploadResume(userId, resumeTitle, content));
    }

    @Operation(summary = "简历列表（分页）")
    @GetMapping("/list")
    public Result<PageResult<ResumeVO>> listResumes(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(resumeService.listResumes(userId, page, size));
    }

    @Operation(summary = "简历详情")
    @GetMapping("/{resumeId}")
    public Result<ResumeVO> getResumeDetail(
            Authentication authentication,
            @PathVariable Long resumeId) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(resumeService.getResumeDetail(userId, resumeId));
    }

    @Operation(summary = "删除简历")
    @DeleteMapping("/{resumeId}")
    public Result<Void> deleteResume(
            Authentication authentication,
            @PathVariable Long resumeId) {
        Long userId = (Long) authentication.getPrincipal();
        resumeService.deleteResume(userId, resumeId);
        return Result.success();
    }

    @Operation(summary = "优化简历（异步）")
    @PostMapping("/{resumeId}/optimize")
    public Result<Void> optimizeResume(
            Authentication authentication,
            @PathVariable Long resumeId) {
        Long userId = (Long) authentication.getPrincipal();
        resumeService.optimizeResume(userId, resumeId);
        return Result.success();
    }

    /**
     * 从PDF文件提取文本
     */
    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 从PDF字节数组提取文本
     */
    private String extractTextFromPdfBytes(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
