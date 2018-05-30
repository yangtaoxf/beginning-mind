/*
 * Created by IntelliJ IDEA File Templates.
 */

package com.spldeolin.beginningmind.controller;

import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.spldeolin.beginningmind.controller.manager.ImageManager;

/**
 * 图片
 *
 * @author Deolin 2018/05/28
 */
@RestController
@RequestMapping("/image")
@Validated
public class ImageController {

    @Autowired
    private ImageManager imageManager;

    /**
     * 普通上传
     */
    @PostMapping("/upload")
    Object create(@RequestParam("image") @NotNull MultipartFile multipartFile) {
        return imageManager.upload(multipartFile);
    }

}