<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:xs="http://www.w3.org/2001/XMLSchema"
      xmlns:dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
      xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
      exclude-result-prefixes="xs"
      version="2.0">

    <xsl:output indent="yes"/>
    <xsl:template match="/">
        <cerif4Ref>
            <c4rREFData hesaID="">
        <xsl:variable name="batch-ID"><xsl:value-of select="generate-id()"/></xsl:variable>
        <c4rResearchActiveStaff>
        <xsl:for-each select="oai/oai_dc:dc/*">
            <xsl:sort select="."/>
            <xsl:if test="name()='dc:creator' and not(.=preceding-sibling)">
                <c4rResearchActiveStaffMember>
                    <xsl:attribute name="c4rHESAStaffIdentifier">
                        <xsl:value-of select="$batch-ID"/>-author<xsl:number format="001" level="any"/>
                    </xsl:attribute>
                    <c4rLastName><xsl:value-of select="substring-before(.,',')"/></c4rLastName>
                    <c4rInitials><xsl:value-of select="substring-after(.,',')"/></c4rInitials>
                </c4rResearchActiveStaffMember>     
            </xsl:if>
        </xsl:for-each>
        <xsl:for-each select="./*">
            <xsl:if test="name()='dc:contributor'">
                <c4rResearchActiveStaffMember>
                    <xsl:attribute name="c4rHESAStaffIdentifier">
                        <xsl:value-of select="$batch-ID"/>-co-author<xsl:number format="001" level="any"/>
                    </xsl:attribute>
                    <c4rLastName><xsl:value-of select="substring-before(.,',')"/></c4rLastName>
                    <c4rInitials><xsl:value-of select="substring-after(.,',')"/></c4rInitials>
                </c4rResearchActiveStaffMember>
            </xsl:if>
        </xsl:for-each>
        </c4rResearchActiveStaff>   
        <xsl:for-each select="oai/oai_dc:dc">
            <c4rResearchOutputs>
                <c4rResearchOuput>
                <xsl:attribute name="c4rOutputID"><xsl:value-of select="$batch-ID"/>-output<xsl:number format="001" level="any"/></xsl:attribute>
                <xsl:attribute name="c4rStaffIdentifier">
                <xsl:for-each select="./*">
                    <xsl:if test="name()='dc:creator'">
                        <xsl:value-of select="$batch-ID"/>-author-<xsl:number format="001" level="any"/>
                    </xsl:if>
                    <xsl:if test="following-sibling::*[name()='dc:creator']">
                        <xsl:text> </xsl:text>
                    </xsl:if>
                    </xsl:for-each>
                </xsl:attribute>
                <c4rYear><xsl:value-of select="substring(./*[name()='dc:date'], 1,4)"/></c4rYear>
                <c4rLongTitle><xsl:value-of select="./*[name()='dc:title']"></xsl:value-of></c4rLongTitle>
                <c4rShortTitle/>
                <c4rPublisher><xsl:value-of select="./*[name()='dc:publisher']"/></c4rPublisher>
                <c4rPublicationDate><xsl:value-of select="./*[name()='dc:date']"/></c4rPublicationDate>
                <c4rEnglishAbstract>
                    <c4rParagraph><xsl:value-of select="./*[name()='dc:description']"></xsl:value-of></c4rParagraph>
                </c4rEnglishAbstract>
                <xsl:for-each select="./*">
                    <xsl:if test="name()='dc:contributor'">
                        <c4rCoAuthor>
                        <c4rCoAuthorName>
                        <xsl:attribute name="c4rCoAuthorID">
                            <xsl:text> </xsl:text><xsl:value-of select="$batch-ID"/>-co-author<xsl:number format="001" level="any"/>
                        </xsl:attribute>
                        <xsl:value-of select="."/>
                        </c4rCoAuthorName>
                        </c4rCoAuthor>
                    </xsl:if>
                </xsl:for-each>     
                    </c4rResearchOuput>
                    </c4rResearchOutputs>
            </xsl:for-each>
            </c4rREFData>
        </cerif4Ref>
    </xsl:template>
</xsl:stylesheet>

