package org.kindura.rules;

import org.kindura.rules.*;

public class simpleTest
{
	public static void main( String args[] )
	{
		StorageProvider sp = new StorageProvider("/home/rd73/Work/Kindura/RuleEngine/org/kindura/rules/Amazon.xml");
		System.out.println("Name: " + sp.name );
		System.out.println("Desc: " + sp.description );
		System.out.println("Constraints:");
		for (StorageProviderConstraint c : sp.getConstraints() )
		{
			System.out.println("Constraint: " + c.getType() + " = " + c.getValue() );
		}
		StorageProvider sp2 = new StorageProvider("/home/rd73/Work/Kindura/RuleEngine/org/kindura/rules/Azure.xml");
		System.out.println("Name: " + sp2.name );
		System.out.println("Desc: " + sp2.description );
		System.out.println("Constraints:");
		for (StorageProviderConstraint c : sp2.getConstraints() )
		{
			System.out.println("Constraint: " + c.getType() + " = " + c.getValue() );
		}
	}
}
