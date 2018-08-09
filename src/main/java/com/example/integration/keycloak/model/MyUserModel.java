package com.example.integration.keycloak.model;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

public class MyUserModel extends AbstractUserAdapterFederatedStorage  {
	
	private String  keycloakId=null;
	private ComponentModel componentModel;

	@Override
	public String getEmail() {
		return getFirstAttribute(EMAIL);
	}

	@Override
	public void setEmail(String email) {
		setSingleAttribute(EMAIL, email);
	}

	public MyUserModel(KeycloakSession session, RealmModel realmModel, ComponentModel componentModel, String username) {
		super(session, realmModel, componentModel);
		this.componentModel=componentModel;
		this.keycloakId = StorageId.keycloakId(componentModel, String.valueOf(username));
	}
	
	@Override
	public String getId() {
		// @see https://keycloak.gitbooks.io/documentation/server_development/topics/user-storage/model-interfaces.html
		return keycloakId;
	}
	
	public ComponentModel getComponentModel() {
		return componentModel;
	}

	@Override
	public String getUsername() {
		return getFirstAttribute(USERNAME);
	}

	@Override
	public void setUsername(String username) {
		setSingleAttribute(USERNAME, username);		
	}
	
}
