# Maintenance

<!--Writerside adds this topic when you create a new documentation project.
You can use it as a sandbox to play with Writerside features, and remove it from the TOC when you don't need it anymore.-->

## Add new topics

You can create empty topics, or choose a template for different types of content that contains some boilerplate
structure to help you get started:

![Create new topic options](new_topic_options.png){ width=290 }{border-effect=line}

## IntelliJ version updates

%product% supports two types of markup: Markdown and XML.
When you create a new help article, you can choose between two topic types, but this doesn't mean you have to stick to a
single format.
You can author content in Markdown and extend it with semantic attributes or inject entire XML elements.

## Dependencies

A+ courses plugin is dependent on the following:

* Scala
* Ktor
* Qodana

## Errors

### Tabs

To add switchable content, you can make use of tabs (inject them by starting to type `tab` on a new line):

<tabs>
    <tab title="Markdown">
        <code-block lang="plain text">![Alt Text](new_topic_options.png){ width=450 }</code-block>
    </tab>
    <tab title="Semantic markup">
        <code-block lang="xml">
            <![CDATA[<img src="new_topic_options.png" alt="Alt text" width="450px"/>]]></code-block>
    </tab>
</tabs>

### Collapsible blocks

Apart from injecting entire XML elements, you can use attributes to configure the behavior of certain elements.
For example, you can collapse a chapter that contains non-essential information:

#### Supplementary info {collapsible="true"}

Content under a collapsible header will be collapsed by default,
but you can modify the behavior by adding the following attribute:
`default-state="expanded"`

### Convert selection to XML

If you need to extend an element with more functions, you can convert selected content from Markdown to semantic markup.
For example, if you want to merge cells in a table, it's much easier to convert it to XML than do this in Markdown.
Position the caret anywhere in the table and press <shortcut>Alt+Enter</shortcut>:

<img src="convert_table_to_xml.png" alt="Convert table to XML" width="706" border-effect="line"/>

## Feedback and support

Please report any issues, usability improvements, or feature requests to our
<a href="https://youtrack.jetbrains.com/newIssue?project=WRS">YouTrack project</a>
(you will need to register).

You are welcome to join our
<a href="https://jb.gg/WRS_Slack">public Slack workspace</a>.
Before you do, please read our [Code of conduct](https://www.jetbrains.com/help/writerside/writerside-code-of-conduct.html).
We assume that you’ve read and acknowledged it before joining.

You can also always email us at [writerside@jetbrains.com](mailto:writerside@jetbrains.com).

<seealso>
    <category ref="wrs">
        <a href="https://www.jetbrains.com/help/writerside/markup-reference.html">Markup reference</a>
        <a href="https://www.jetbrains.com/help/writerside/manage-table-of-contents.html">Reorder topics in the TOC</a>
        <a href="https://www.jetbrains.com/help/writerside/local-build.html">Build and publish</a>
        <a href="https://www.jetbrains.com/help/writerside/configure-search.html">Configure Search</a>
    </category>
</seealso>