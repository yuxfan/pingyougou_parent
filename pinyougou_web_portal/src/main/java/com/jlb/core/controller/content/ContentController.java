package com.jlb.core.controller.content;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.pojo.ad.Content;
import com.jlb.core.service.content.ContentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("content")
public class ContentController {

    @Reference
    private ContentService contentService;

    @RequestMapping("/findByCategoryId.do")
    public List<Content> findByCategoryId(Long categoryId){
        return contentService.findByCategoryId(categoryId);
    }
}
