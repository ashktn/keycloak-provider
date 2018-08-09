package com.example.integration.keycloak.model;

import java.util.Set;

import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;

public class MyUserRoleModel implements RoleModel {
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isScopeParamRequired() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setScopeParamRequired(boolean scopeParamRequired) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isComposite() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addCompositeRole(RoleModel role) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeCompositeRole(RoleModel role) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<RoleModel> getComposites() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isClientRole() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getContainerId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RoleContainerModel getContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasRole(RoleModel role) {
		// TODO Auto-generated method stub
		return false;
	}

}
