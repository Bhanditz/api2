package eu.europeana.api2demo.web.service;

import eu.europeana.api2demo.web.model.TagCloud;
import eu.europeana.api2demo.web.model.UserSavedItems;
import eu.europeana.api2demo.web.model.UserSearches;
import eu.europeana.api2demo.web.model.UserTags;

public interface Api2UserService {

	// SAVED ITEMS
	UserSavedItems getSavedItems();

	boolean createSavedItem(String id);

	boolean deleteSavedItem(Long id);

	// TAGS
	UserTags getTags(String filter);
	
	TagCloud createTagCloud();

	boolean createTag(String id, String tag);

	boolean deleteTag(Long id);
	
	// SAVED SEARCHES
	UserSearches getSavedSearches();

	boolean deleteSavedSearche(Long id);
	
}
