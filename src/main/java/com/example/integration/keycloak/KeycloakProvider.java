package com.example.integration.keycloak;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordUserCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.database.util.DBUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 
 * @see - https://github.com/keycloak/keycloak-quickstarts/
 */
public class KeycloakProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator, UserQueryProvider, CredentialInputUpdater {

	private KeycloakSession session = null;
	private ComponentModel componentModel = null;
	
	private DBUtils dbUtils;

	private static final Logger logger = LoggerFactory.getLogger(KeycloakProvider.class);
	public static final String MY_CLIENT = "my-client";

	public KeycloakProvider(KeycloakSession session, ComponentModel componentModel) {
		this.session = session;
		this.componentModel = componentModel;
		dbUtils=new DBUtils(session, componentModel);
		try {
			dbUtils.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public KeycloakSession getSession() {
		return session;
	}
	
	public ComponentModel getComponentModel() {
		return componentModel;
	}

	@Override
	public void preRemove(RealmModel realm) {
		UserStorageProvider.super.preRemove(realm);
	}

	@Override
	public void preRemove(RealmModel realm, GroupModel group) {
		UserStorageProvider.super.preRemove(realm, group);
	}

	@Override
	public void preRemove(RealmModel realm, RoleModel role) {
		UserStorageProvider.super.preRemove(realm, role);
	}

	@Override
	public void close() {
		logger.info("Closing Provider -----------");
	}

	@Override
	public UserModel getUserByEmail(String email, RealmModel realmModel) {
		logger.info("Calling getUserByEmail: {} ", email);
		return null;
	}

	@Override
	public UserModel getUserById(String id, RealmModel realmModel) {
		logger.info("Calling getUserById: {}", id);
		String username = StorageId.externalId(id);
		return this.getUserByUsername(username, realmModel);
	}

	@Override
	public UserModel getUserByUsername(String userName, RealmModel realmModel) {
		UserModel model = null;
		logger.info("Calling getUserByUsername: {}", userName);
		
		try {
			dbUtils.setRealmModel(realmModel);
			model = dbUtils.findUserByUserName(userName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}

	@Override
	public int getUsersCount(RealmModel realm) {
		logger.info("Calling getUserCount");
		return 0;
	}

	@Override
	public List<UserModel> getUsers(RealmModel realm) {
		logger.info("Calling getUsers (realm)");
		return getUsers(realm, -1, -1);
	}

	@Override
	public List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {
		logger.info("Calling getUsers (realm, firstResult: {}, maxResults: {})", firstResult, maxResults);
		List<UserModel> users = new LinkedList<>();
		try {
			dbUtils.setRealmModel(realm);
			users = dbUtils.getUsers(firstResult, maxResults);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return users;
	}

	@Override
	public List<UserModel> searchForUser(String search, RealmModel realm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm, int firstResult, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<UserModel> searchForUserByUserAttribute(String attrName, String attrValue, RealmModel realm) {
		return null;
	}	

	@Override
	public boolean supportsCredentialType(String credentialType) {
		return CredentialModel.PASSWORD.equals(credentialType);
	}

	@Override
	public boolean updateCredential(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
		if (credentialInput.getType().equals(CredentialModel.PASSWORD)) {
			String encryptedPassword = ecryptPassword(((PasswordUserCredentialModel) credentialInput).getValue());
			dbUtils.setPassword(MY_CLIENT, userModel.getUsername(), encryptedPassword);
		}
		return true;
	}

	@Override
	public void disableCredentialType(RealmModel realmModel, UserModel userModel, String s) {

	}

	@Override
	public Set<String> getDisableableCredentialTypes(RealmModel realmModel, UserModel userModel) {
		return Collections.emptySet();
	}

	@Override
	public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
		return supportsCredentialType(credentialType) && dbUtils.getPassword(user.getFirstAttribute("client"), user.getUsername()) != null;
	}

	@Override
	public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
		if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) return false;
	        
	 	UserCredentialModel cred = (UserCredentialModel)input;
        logUserRoles(user);
        String userPassword = dbUtils.getPassword(user.getFirstAttribute("client"),user.getUsername());
        String providedPassword = ecryptPassword(cred.getValue());
        return providedPassword != null && providedPassword.equals(userPassword);
	}
	
	private void logUserRoles(UserModel user) {
		if(user!=null) {
			Set<RoleModel> userRoles = user.getRoleMappings();
			if(userRoles!=null) {
				logger.info("Logging User Roles");
				for(RoleModel ur : userRoles){
					logger.info("{}", ur.getName());
				}
			}
		}
	}

	private String ecryptPassword(String value) {
		String password = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] digested = digest.digest(value.getBytes());
			if(digested != null){
				password = java.util.Base64.getEncoder().encodeToString(digested);
			}
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return password;
	}
	
}
