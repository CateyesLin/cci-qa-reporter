<!DOCTYPE html>
<html>
    <head>
        <title>Indoor Report</title>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <style type="text/css">
		.report, .report th, .report td {
           border: 1px solid gray;
        }
        .report th {
        	background-color: #69F;
        }
        .report td {
        	vertical-align: top;
        }
        .report tr:nth-child(even) {
            background-color: #DDD;
        }
        p.name {
            margin-top: 2px;
            margin-bottom: 2px;
        }
        </style>
    </head>
    <body>
    	<h3>Indoor Report</h3>
        <table class="report">
            <thead>
                <tr>
                    <th>Motive</th>
                    <th>Conditions</th>
                    <th>Assessment Criteria</th>
<#list devices as device>
                    <th>${device}</th>
</#list>
                </tr>
            </thead>
            <tbody>
<#list scenes as scene>
            <tr>
                <td>${scene.name}</td>
                <td>${scene.condition}</td>
                <td>${scene.criteria}</td>
<#list scene.devicesPhoto?keys as key>
                <td>
<#list scene.devicesPhoto[key] as photo>
					<p class="name">${photo.getName()}</p>
                    <a target="_blank" href="${photo.getPath()}">
                        <img src="thumb/${photo.getPath()?substring(4)}"/>
                    </a>
                    <br/>
</#list>
                </td>
</#list>
            </tr>
</#list>
            </tbody>
        </table>
    </body>
</html>