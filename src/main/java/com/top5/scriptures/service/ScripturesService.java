package com.top5.scriptures.service;

import java.util.List;
import java.util.Set;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.top5.scriptures.custom.UserAlreadyExistsException;
import com.top5.scriptures.model.BibleVersion;
import com.top5.scriptures.model.Scripture;
import com.top5.scriptures.model.User;

@Service
public interface ScripturesService {
	
	public UserDetails loadUserByUsername(String username);
	
	public User registerUser(User user) throws UserAlreadyExistsException;
	
	public List<BibleVersion> getBibleVersions();
	
	public Set<String> loadCategories();
	
	public boolean updateUserSettings(User user);
	
	public List<Scripture> loadScriptures(User user);

}
