<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:pkg="http://expath.org/ns/pkg"
                pkg:import-uri="[[ http://example.org/project/pipe.xproc ]]"
                exclude-inline-prefixes="p c pkg"
                version="1.0">

   <p:input port="source">
      <p:inline>
         <doc>Hello world!</doc>
      </p:inline>
   </p:input>

   <p:output port="result"/>

   <p:identity/>

</p:declare-step>
