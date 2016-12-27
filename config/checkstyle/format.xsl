<!--
  ~ Copyright (c) 2016 Duna Open Source Project
  ~ Ministério do Planejamento, Desenvolvimento de Gestão
  ~ República Federativa do Brasil
  ~
  ~ This file is part of the Duna Project.
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
    <xsl:output method="html" indent="yes"/>
    <xsl:decimal-format decimal-separator="." grouping-separator="," />

    <xsl:key name="files" match="file" use="@name" />

    <!-- Checkstyle XML Style Sheet by Stephane Bailliez <sbailliez@apache.org>         -->
    <!-- Part of the Checkstyle distribution found at http://checkstyle.sourceforge.net -->
    <!-- Usage (generates checkstyle_report.html):                                      -->
    <!--    <checkstyle failonviolation="false" config="${check.config}">               -->
    <!--      <fileset dir="${src.dir}" includes="**/*.java"/>                          -->
    <!--      <formatter type="xml" toFile="${doc.dir}/checkstyle_report.xml"/>         -->
    <!--    </checkstyle>                                                               -->
    <!--    <style basedir="${doc.dir}" destdir="${doc.dir}"                            -->
    <!--            includes="checkstyle_report.xml"                                    -->
    <!--            style="${doc.dir}/checkstyle-noframes-sorted.xsl"/>                 -->

    <xsl:template match="checkstyle">
        <html>
            <head>
                <style type="text/css">
                    .bannercell {
                        border: 0px;
                        padding: 0px;
                    }
                    body {
                        margin-left: 10;
                        margin-right: 10;
                        font-family: -apple-system, BlinkMacSystemFont,
                            "Ubuntu", "Segoe UI", Helvetica, Arial, sans-serif,
                            "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol";
                        font-weight: normal;
                        font-size: 0.8em;
                        background-color:#FFFFFF;
                        color:#000000;
                    }
                    .a td {
                        background: #efefef;
                    }
                    .b td {
                        background: #fff;
                    }
                    th, td {
                        text-align: left;
                        vertical-align: top;
                    }
                    th {
                        font-weight:bold;
                        background: #ccc;
                        color: black;
                    }
                    table, th, td {
                        font-size:100%;
                        border: none
                    }
                    table.log tr td, tr th {

                    }
                    h2 {
                        font-weight:bold;
                        font-size:140%;
                        margin-bottom: 5;
                    }
                    h3 {
                        font-size: 1.2em;
                        font-weight: bold;
                        text-decoration: none;
                        padding: 5px;
                        margin-bottom: 0;
                    }
                    a, a:visited {
                        color: blue;
                    }
                </style>
            </head>
            <body>
                <a name="top"></a>
                <table border="0" cellpadding="0" cellspacing="0" width="100%">
                    <tr>
                        <td class="bannercell" rowspan="2">
                        </td>
                        <td class="text-align:right"><h2>Duna CheckStyle Audit</h2></td>
                    </tr>
                </table>
                <hr size="1" width="100%" align="left"/>

                <!-- Summary part -->
                <xsl:apply-templates select="." mode="summary"/>

                <!-- Package List part -->
                <xsl:apply-templates select="." mode="filelist"/>

                <!-- For each package create its part -->
                <xsl:apply-templates select="file[@name and generate-id(.) = generate-id(key('files', @name))]" />
            </body>
        </html>
    </xsl:template>

    <xsl:template match="checkstyle" mode="filelist">
        <xsl:choose>
            <xsl:when test="count(file/error) &gt; 0">
                <h3>Files</h3>
                <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
                    <tr>
                        <th>Name</th>
                        <th>Errors</th>
                    </tr>
                    <xsl:for-each select="file[@name and generate-id(.) = generate-id(key('files', @name))]">
                        <xsl:sort data-type="number" order="descending" select="count(key('files', @name)/error)"/>
                        <xsl:variable name="currentName" select="@name" />
                        <xsl:variable name="errorCount" select="count(key('files', @name)/error)"/>
                        <xsl:if test="$errorCount &gt; 0">
                            <tr>
                                <xsl:call-template name="alternated-row"/>
                                <td><a href="#f-{@name}"><xsl:value-of select="@name"/></a></td>
                                <td><xsl:value-of select="$errorCount"/></td>
                            </tr>
                        </xsl:if>
                    </xsl:for-each>
                </table>
                <hr size="1" width="100%" align="left"/>
            </xsl:when>
            <xsl:otherwise>
                <h3>No code style errors found in <xsl:value-of
                    select="count(file[@name and generate-id(.) = generate-id(key('files', @name))])"/> files</h3>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template match="file">
        <xsl:if test="count(key('files', @name)/error) &gt; 0">
            <a name="f-{@name}"></a>
            <xsl:variable name="fileName" select="@name"/>
            <h3><xsl:value-of select="@name"/></h3>

            <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
                <tr>
                    <th>Line</th>
                    <th>Error Description</th>
                </tr>
                <xsl:for-each select="key('files', @name)/error">
                    <xsl:sort data-type="number" order="ascending" select="@line"/>
                    <tr>
                        <xsl:call-template name="alternated-row"/>
                        <td><xsl:value-of select="@line"/></td>
                        <td><xsl:value-of select="@message"/></td>
                    </tr>
                </xsl:for-each>
            </table>
            <a href="#top">Back to top</a>
        </xsl:if>
    </xsl:template>


    <xsl:template match="checkstyle" mode="summary">
        <h3>Summary</h3>
        <xsl:variable name="fileCount" select="count(file[@name and generate-id(.) = generate-id(key('files', @name))])"/>
        <xsl:variable name="errorCount" select="count(file/error)"/>
        <table class="log" border="0" cellpadding="5" cellspacing="2" width="100%">
            <tr>
                <th>Files</th>
                <th>Errors</th>
            </tr>
            <tr>
                <xsl:call-template name="alternated-row"/>
                <td><xsl:value-of select="$fileCount"/></td>
                <td><xsl:value-of select="$errorCount"/></td>
            </tr>
        </table>
        <hr size="1" width="100%" align="left"/>
    </xsl:template>

    <xsl:template name="alternated-row">
        <xsl:attribute name="class">
            <xsl:if test="position() mod 2 = 1">a</xsl:if>
            <xsl:if test="position() mod 2 = 0">b</xsl:if>
        </xsl:attribute>
    </xsl:template>
</xsl:stylesheet>

