package com.example.integration.keycloak.model;

import org.apache.commons.dbutils.ResultSetHandler;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MyUserResultSetHandler implements ResultSetHandler<MyUserModel> {
	
	private KeycloakSession session;
	private RealmModel realmModel;
	private ComponentModel componentModel;

	public MyUserResultSetHandler(KeycloakSession session, RealmModel realmModel, ComponentModel componentModel) {
		this.session = session;
		this.realmModel = realmModel;
		this.componentModel = componentModel;
	}

	@Override
	public MyUserModel handle(ResultSet resultSet) throws SQLException {
		MyUserModel user = null;
		try {
			user = transformResultSetToUser(resultSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return user;
	}

	private MyUserModel transformResultSetToUser(ResultSet resultSet) throws SQLException {
		MyUserModel userModel = null;

		if (resultSet != null && resultSet.next()) {
			String username = resultSet.getString("username");
			String email = resultSet.getString("email");
			userModel = new MyUserModel(this.session, this.realmModel, this.componentModel, username);
			userModel.setUsername(username);
			userModel.setEmail(email);
			userModel.setFirstName(resultSet.getString("firstName"));
			userModel.setLastName(resultSet.getString("lastName"));
			Short activeFlag = resultSet.getShort("enabled");
			boolean active = activeFlag == 1;
			userModel.setEnabled(active);
			
		}
		return userModel;
	}

}
