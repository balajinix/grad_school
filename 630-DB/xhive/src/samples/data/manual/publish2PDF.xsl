<?xml version="1.0"?>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  	xmlns:fo="http://www.w3.org/1999/XSL/Format">
 
	<xsl:output method="xml" />

	<xsl:template match ="/">

		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

    			<!-- defines page layout -->
    			<fo:layout-master-set>
      				<fo:simple-page-master 
					master-name="simple"
                    			page-height="29.7cm" 
                    			page-width="21cm"
                    			margin-top="1.5cm" 
                    			margin-bottom="2cm" 
                    			margin-left="1.5cm" 
                    			margin-right="1.5cm">
        				<fo:region-body margin-top="3cm"/>
        				<fo:region-before extent="1.5cm"/>
        				<fo:region-after extent="1.5cm"/>
      				</fo:simple-page-master>
    			</fo:layout-master-set>

			<!-- master --> 
			<fo:page-sequence master-name="simple">

      				<!-- header --> 
      				<fo:static-content flow-name="xsl-region-before">
        				<fo:block font-size="10pt" font-family="sans-serif" text-align="end">
          					UN Charter - Chapter <xsl:value-of select="/chapter/@number"/> - Page <fo:page-number/>
        				</fo:block>
      				</fo:static-content> 
      
				<!-- body --> 
      				<fo:flow flow-name="xsl-region-body" font-size="12pt" font-family="sans-serif" line-height="1.5">
        				<xsl:apply-templates/> 
      				</fo:flow>
    			</fo:page-sequence>  
  		</fo:root>
	</xsl:template>     

     
	<xsl:template match="chapter">
		<fo:block font-size="20pt" text-align="center" color="red" space-after="20pt">
			<xsl:value-of select="title"/>
		</fo:block>
		<xsl:apply-templates/> 
	</xsl:template>

	<xsl:template match="section">
		<fo:block font-size="16pt" space-before="16pt">
			<xsl:value-of select="title"/>
		</fo:block>
		<xsl:apply-templates/> 
	</xsl:template>

	<xsl:template match="article">
		<fo:block font-size="14pt" font-weight="bold" space-before="16pt">
			Article <xsl:value-of select="@number"/>
		</fo:block>
		<xsl:apply-templates/> 
	</xsl:template>

	<xsl:template match="para">		
		<fo:block>
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	<xsl:template match="list">
		<fo:list-block>
			<xsl:apply-templates/>
		</fo:list-block>
	</xsl:template>

	<xsl:template match="item">
		<fo:list-item>
			<fo:list-item-label>  
				<fo:block>&#x2022;</fo:block>  
			</fo:list-item-label> 
			<fo:list-item-body>
				<xsl:apply-templates/>
			</fo:list-item-body>
		</fo:list-item>
	</xsl:template>

	<xsl:template match="*|@*">
		<!-- ignore -->
	</xsl:template>


</xsl:stylesheet>














