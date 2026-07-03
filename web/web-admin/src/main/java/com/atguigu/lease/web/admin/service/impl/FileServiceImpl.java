package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.minio.MinioProperties;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.web.admin.service.FileService;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioProperties properties;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L; // 5MB

    @Override
    public String upload(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

            // 校验文件是否为空
            if (file == null || file.isEmpty()) {
                throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "上传文件不能为空");
            }

            // 校验文件大小
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "文件大小不能超过5MB");
            }

            // 校验文件类型
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "仅支持 jpg/png/gif/webp 格式的图片");
            }

            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(properties.getBucketName())
                            .build());

            if (!bucketExists) {
                // 创建bucket
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(properties.getBucketName())
                                .build()
                );

                // 设置bucket策略为公开读
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(properties.getBucketName())
                                .config(createBucketPolicyConfig(properties.getBucketName()))
                                .build()
                );
            }

            // 生成文件名：日期/UUID.扩展名（避免中文和空格导致 URL 编码问题）
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String filename = new SimpleDateFormat("yyyyMMdd").format(new Date()) +
                    "/" + UUID.randomUUID() + extension;

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .object(filename)
                            .contentType(file.getContentType())
                            .build()
            );

            // 返回文件访问URL
            return String.join("/", properties.getEndpoint(), properties.getBucketName(), filename);
    }
    private String createBucketPolicyConfig(String bucketName) {

        return """
            {
              "Statement" : [ {
                "Action" : "s3:GetObject",
                "Effect" : "Allow",
                "Principal" : "*",
                "Resource" : "arn:aws:s3:::%s/*"
              } ],
              "Version" : "2012-10-17"
            }
            """.formatted(bucketName);
    }
}

