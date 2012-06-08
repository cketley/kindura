package org.kindura;
import org.kindura.StorageProviderConstraintEnum.*;

public class StorageProviderConstraint
{
	private StorageProviderConstraintEnum type;
	private String constraintValue = "";
	
	public StorageProviderConstraint( StorageProviderConstraintEnum type, String value )
	{
		this.type = type;
		this.constraintValue = value;
	}

	public StorageProviderConstraintEnum getType()
	{
		return type;
	}

	public String getValue()
	{
		return constraintValue;
	}
}
