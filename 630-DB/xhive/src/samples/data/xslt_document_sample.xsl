<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                              xmlns:xlink="http://www.w3.org/1999/xlink">

  <!--
    Small sample XSLT document to show to use the
      document('xhive:/path/to/library/or/document[#xquery]')
    function from our XSLT transformer
    
    To use it in a sample, you could e.g. place the code
      firstDocument = charterLib.parseDocument(new File(SampleProperties.baseDir + "un_bodies.xml").toURL(), XhiveLibraryIf.PARSER_NO_VALIDATION | XhiveLibraryIf.PARSER_NAMESPACES_ENABLED);
      xslDocument = charterLib.parseDocument(new File(SampleProperties.baseDir + "../xslt_document_sample.xsl").toURL(), XhiveLibraryIf.PARSER_NO_VALIDATION | XhiveLibraryIf.PARSER_NAMESPACES_ENABLED);
    in the Publish2HTML.java sample, just before the line 
      String result = transformer.transformToString(firstDocument, xslDocument);
      
    Other examples of document selects could be:
     - Get all content from the library:
          select="document('xhive:/UN Charter#&lt;libcontent&gt;{/*}&lt;/libcontent&gt;')"
     - Get all titles from all documents:
		      select="document('xhive:/UN Charter#&lt;newdoc&gt;{for $i in / return 
		        &lt;title&gt; {base-uri($i)} &lt;/title&gt;  }&lt;/newdoc&gt;')"/>

    Notes:
     - Make sure you do not have a document(...) selector in a template that matches '/', as then
       you might get a recursive load-loop.
     - If you use element constructors in the query-part of the document, remember that data
       will first be copied in the query.
     - Remember that only the first result of the query will be the result of the document
       expression.
  
  -->

	<xsl:output method="xml" indent="yes"/>

	<xsl:template xmlns:xh="http://www.x-hive.com" match="xh:un_body">
		<xsl:apply-templates select="document(concat('xhive:', @xlink:href))"/>
	</xsl:template>

	<xsl:template match="title">
		<xlink:importedtitle><xsl:apply-templates/></xlink:importedtitle>
	</xsl:template>

  <!-- Copy by default -->
  <xsl:template match="*">
      <xsl:call-template name="do-copy"/>
  </xsl:template>

  <xsl:template name="do-copy">
      <xsl:copy>
          <xsl:for-each select="@*">
              <xsl:copy/>
          </xsl:for-each>
          <xsl:apply-templates/>            
      </xsl:copy>
  </xsl:template>


</xsl:stylesheet>

