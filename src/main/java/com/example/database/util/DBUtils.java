package com.example.database.util;

import com.example.integration.keycloak.KeycloakProvider;
import com.example.integration.keycloak.model.MyUserModel;
import com.example.integration.keycloak.model.MyUserResultSetHandler;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class DBUtils {

	private DataSource dataSource;
	
	private KeycloakSession session;
	private RealmModel realmModel;
	private ComponentModel componentModel;

	
	private Logger logger=LoggerFactory.getLogger(DBUtils.class);
	private static Properties queries = new Properties();
	
	static {
		try {
			queries.load(DBUtils.class.getClassLoader().getResourceAsStream("./queries.properties"));			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public DBUtils(KeycloakSession session, ComponentModel componentModel) {
		this.session=session;
		this.componentModel=componentModel;	
	}
	
	public void setRealmModel(RealmModel realmModel) {
		this.realmModel = realmModel;
	}

	public DBUtils(KeycloakSession session, RealmModel realmModel, ComponentModel componentModel) {
		this.session=session;
		this.realmModel=realmModel;
		this.componentModel=componentModel;		
	}
	
	public MyUserModel findUserByUserName(String userName) throws Exception {
		
		MyUserModel user = null;
		//find user in mobile_user db
		ClientModel mobileClient = realmModel.getClientByClientId(KeycloakProvider.MY_CLIENT);
		user = findUserByClientAndUserName(mobileClient, userName);
		return user;
	}

	private MyUserModel findUserByClientAndUserName(ClientModel client, String userName) throws SQLException {
		MyUserModel user = null;

		QueryRunner qr = new QueryRunner(dataSource);
		String sql = getSQLQuery(client.getClientId(), "findByUserName");
		user = qr.query(sql, new MyUserResultSetHandler(session, realmModel, componentModel), userName);
		if (user != null) {
			assignRolesToUser(user, client);
			user.setSingleAttribute("client",client.getClientId());
		}
		return user;
	}
	
	private void assignRolesToUser(MyUserModel user, ClientModel client) throws SQLException {
		Set<RoleModel> roles = client.getRoles();
		Set<RoleModel> userRoles = user.getClientRoleMappings(client);
		
		//Delete all user roles and then re-assign to avoid Unique Constraint Violation exception
		for(RoleModel ur : userRoles) {
			user.deleteRoleMapping(ur);
		}
		
		List<String> externalRoles = getExternalUserRoles(user, client);
		if(externalRoles!=null && externalRoles.size() > 0) {
			for(String r : externalRoles) {
				for(RoleModel role : roles) {
					if(role.getName().equals(r)) {
						logger.info("Assigning Role: {}", role.getName());
		 //				user.getRoleMappings().add(role);
						user.grantRole(role);
					}
				}
			}
		}
	}

	private List<String> getExternalUserRoles(MyUserModel user, ClientModel client) throws SQLException {
		QueryRunner qr = new QueryRunner(dataSource, true);
		String sql = getSQLQuery(client.getClientId(), "getUserRoles");
		List<String> userRoles = qr.query(sql, new ColumnListHandler<String>("role"), user.getUsername());
		return userRoles;
	}

	public MyUserModel getUserById(String userId) throws SQLException {
		QueryRunner qr = new QueryRunner(dataSource);
		String sql = "Select id as userId, full_name as firstName, principal_name as username, password as password, email as email, is_active as enabled from user where id = ? and is_active=1";
		MyUserModel user = qr.query(sql, new MyUserResultSetHandler(session, realmModel, componentModel), userId);
		return user;
	} 

	public void init() throws NamingException {
		logger.info("initializing DBUtils");
		dataSource=(DataSource) new InitialContext().lookup("java:/jboss/datasources/mykeycloakds");
	}
	
	private String getSQLQuery(String clientName, String keyword) { 
		String sql = queries.getProperty(new StringBuilder(clientName).append('.').append(keyword).toString());
		return sql;
	}

	public void setPassword(String clientName, String username, String password) {
		QueryRunner qr = new QueryRunner(dataSource);
		String sql = null;

		sql = getSQLQuery(clientName, "setPasswordByUsername");

		try {
			qr.update(sql, password, username);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public String getPassword(String clientName, String username) {
		String password = null;
		QueryRunner qr = new QueryRunner(dataSource);
		String sql = null;
		
		sql = getSQLQuery(clientName, "getPasswordByUserName");
		
		try {
			password = qr.query(sql, new ScalarHandler<String>("password"), username);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return password;
	}

	public List<UserModel> getUsers(int firstResult, int maxResults) throws SQLException {
		List<UserModel> userModels = new LinkedList<UserModel>();
		List<String> users = new ArrayList<>();
		QueryRunner qr = new QueryRunner(dataSource, true);
		String sql = null;
		
		if(firstResult == -1 || maxResults==-1) {
			sql = getSQLQuery(KeycloakProvider.MY_CLIENT, "getAllUsers");
			users.addAll(qr.query(sql, new ColumnListHandler<String>("username")));
			
		} else {
			sql = getSQLQuery(KeycloakProvider.MY_CLIENT, "getUsers");
			users.addAll(qr.query(sql, new ColumnListHandler<String>("username"), firstResult, maxResults));
		}
		
		if(users != null){
			for (String u : users) { 
				userModels.add(new MyUserModel(session, realmModel, componentModel, u));
			}
		}
		
		return userModels;
	}
	
}
