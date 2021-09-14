package com.top5.scriptures.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.top5.scriptures.custom.UserAlreadyExistsException;
import com.top5.scriptures.model.BibleVersion;
import com.top5.scriptures.model.CustomUserDetail;
import com.top5.scriptures.model.Scripture;
import com.top5.scriptures.model.User;
import com.top5.scriptures.repository.ScripturesRepository;
import com.top5.scriptures.service.ScripturesService;


@Component
public class ScriptureServiceImpl implements ScripturesService, UserDetailsService{
	
	@Autowired
	private ScripturesRepository sRepo;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	
	@Override
	public User registerUser(User user) throws UserAlreadyExistsException{
		if (emailExist(user.getEmail())) {
            throw new UserAlreadyExistsException("There is already an account with that email address: "
              + user.getEmail());
        }else {
        	String encodedPwd = passwordEncoder.encode(user.getPassword());
        	user.setPassword(encodedPwd);
        	User regUser = sRepo.createUser(user);
    		return regUser;
        }
		
	}


	@Override
    public CustomUserDetail loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = sRepo.loadUser(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        
        CustomUserDetail customUser = new CustomUserDetail(user);
        customUser.setAuthorities(getAuthorities(user.getRoles()));
        
        return customUser;
    }
	
	private static Set<GrantedAuthority> getAuthorities (List<String> roles) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }
	
	private boolean emailExist(String email) {
        return sRepo.loadUser(email) != null;
    }


	@Override
	public List<BibleVersion> getBibleVersions() {
		return sRepo.getBibleVersions();
	}


	@Override
	public Set<String> loadCategories() {
		return sRepo.loadCategories();
	}


	@Override
	public boolean updateUserSettings(User user) {
		return sRepo.updateUserSettings(user);
	}


	@Override
	public List<Scripture> loadScriptures(User user) {
		return sRepo.loadScriptures(user);
	}
	
	
	
}
