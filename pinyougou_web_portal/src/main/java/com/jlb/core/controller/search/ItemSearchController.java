package com.jlb.core.controller.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.jlb.core.service.search.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("itemsearch")
public class ItemSearchController {

    @Reference
    private ItemSearchService itemSearchService;

    @RequestMapping("/search.do")
    public Map<String,Object> search(@RequestBody Map<String,String> searchMap){
        return itemSearchService.search(searchMap);
    }
}
