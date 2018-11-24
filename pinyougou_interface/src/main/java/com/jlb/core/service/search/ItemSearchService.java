package com.jlb.core.service.search;

import java.util.Map;

public interface ItemSearchService {

    Map<String,Object> search(Map<String,String> searchMap);
}
