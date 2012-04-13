<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
                xmlns:pkg="http://expath.org/ns/pkg"
                exclude-result-prefixes="xs xd pkg"
                version="2.0">

   <pkg:import-uri>[[ http://example.org/project/style.xsl ]]</pkg:import-uri>

   <xd:doc scope="stylesheet">
      <xd:desc>
         <xd:p><xd:b>Created on:</xd:b> [[ 2012-01-01 ]]</xd:p>
         <xd:p><xd:b>Author:</xd:b> [[ Your Name ]]</xd:p>
         <xd:p>Sample stylesheet.</xd:p>
      </xd:desc>
   </xd:doc>

   <xsl:template>
      ...
   </xsl:template>

</xsl:stylesheet>
