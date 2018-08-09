package com.example.integration.keycloak;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class KeycloakProviderFactory implements UserStorageProviderFactory<KeycloakProvider> {

	@Override
	public KeycloakProvider create(KeycloakSession session, ComponentModel componentModel) {
		return new KeycloakProvider(session,componentModel);
	}
	
	@Override
	public String getId() {
		return "Custom-Database-User-Repository";
	}
	
	@Override
    public String getHelpText() {
        return "Custom Database User Storage Provider";
    }

	@Override
    public void close() {
        System.out.println("Closing Keycloak Provider Factory");

    }

}
