module namespace file = "http://expath.org/ns/file";

declare namespace java = "java:org.expath.files.saxon.Files";

declare function file:copy(
   $src  as xs:anyURI,
   $dest as xs:anyURI
) as xs:boolean
{
   java:copy($src, $dest)
};

declare function file:copy(
   $src       as xs:anyURI,
   $dest      as xs:anyURI,
   $overwrite as xs:boolean
) as xs:boolean
{
   java:copy($src, $dest, $overwrite)
};

declare function file:exists(
   $href as xs:anyURI
) as xs:boolean
{
   java:exists($href)
};

declare function file:old-list(
   $href as xs:anyURI
) as element(file:dir)?
{
   java:old-list($href)
};
