<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="html"/>

	<xsl:template match="/">
		<html>
			<head>
				<title>Un Charter - Chapter <xsl:value-of select="/chapter/@number"/></title>
			</head>
			<body>
				<xsl:apply-templates/>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="chapter">
		<H1><xsl:value-of select="title"/></H1>
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="section">
		<H2><xsl:value-of select="title"/></H2>
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="article">
		<H3>Article <xsl:value-of select="@number"/></H3>
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="para">
		<p><xsl:apply-templates/></p>
	</xsl:template>

	<xsl:template match="list">
		<ul><xsl:apply-templates/></ul>
	</xsl:template>

	<xsl:template match="item">
		<li><xsl:apply-templates/></li>
	</xsl:template>

	<xsl:template match="*|@*">
		<!-- ignore -->
	</xsl:template>

</xsl:stylesheet>

