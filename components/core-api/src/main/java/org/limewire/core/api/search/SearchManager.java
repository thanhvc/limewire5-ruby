package org.limewire.core.api.search;

import java.util.List;

import org.limewire.core.impl.search.SearchManagerImpl.SearchWithResults;
import org.limewire.io.GUID;

public interface SearchManager {
    SearchWithResults getSearchByGuid(GUID guid);
    List<SearchWithResults> getAllSearches();
    SearchWithResults createSearchFromQuery(String query);
}
