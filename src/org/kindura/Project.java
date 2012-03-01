package org.kindura;

public class Project
{
    public String name;
    public String funder;

    public Project( String name, String funder )
    {
        this.name = name;
        this.funder = funder;
    }

    public String getFunder()
    {
        return funder;
    }
}
