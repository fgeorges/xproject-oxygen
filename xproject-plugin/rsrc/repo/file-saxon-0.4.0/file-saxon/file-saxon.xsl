<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:file="http://expath.org/ns/file"
                xmlns:java="java:org.expath.files.saxon.Files"
                version="2.0">

   <xsl:function name="file:copy" as="xs:boolean">
      <xsl:param name="src"  as="xs:anyURI"/>
      <xsl:param name="dest" as="xs:anyURI"/>
      <xsl:sequence select="java:copy($src, $dest)"/>
   </xsl:function>

   <xsl:function name="file:copy" as="xs:boolean">
      <xsl:param name="src"       as="xs:anyURI"/>
      <xsl:param name="dest"      as="xs:anyURI"/>
      <xsl:param name="overwrite" as="xs:boolean"/>
      <xsl:sequence select="java:copy($src, $dest, $overwrite)"/>
   </xsl:function>

   <xsl:function name="file:exists" as="xs:boolean">
      <xsl:param name="href" as="xs:anyURI"/>
      <xsl:sequence select="java:exists($href)"/>
   </xsl:function>

   <xsl:function name="file:old-list" as="element(file:dir)?">
      <xsl:param name="href" as="xs:anyURI"/>
      <xsl:sequence select="java:old-list($href)"/>
   </xsl:function>

</xsl:stylesheet>
