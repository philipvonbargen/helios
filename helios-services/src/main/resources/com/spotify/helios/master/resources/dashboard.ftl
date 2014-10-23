<#-- @ftlvariable name="" type="com.spotify.helios.master.resources.DashboardView" -->
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Helios Dashboard</title>

    <!-- Bootstrap core CSS -->
    <link href="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="/css/dashboard.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>

  <body>
    <div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
      <div class="container-fluid">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="${rootUrl}">Helios</a>
        </div>
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav navbar-right">
            <li><a href="https://github.com/spotify/helios/tree/master/docs">Help</a></li>
          </ul>
        </div>
      </div>
    </div>

    <div class="container-fluid">
      <div class="row">
        <div class="col-sm-3 col-md-2 sidebar">
          <ul class="nav nav-sidebar">
            <li class="active"><a href="${rootUrl}">Overview</a></li>
            <li><a href="${hostsUrl}">Hosts</a></li>
            <li><a href="${jobsUrl}">Jobs</a></li>
          </ul>
        </div>
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
          <h1 class="page-header">Overview</h1>

          <h2 class="sub-header">Jobs</h2>
          <div class="table-responsive">
            <table class="table table-striped">
              <thead>
                <tr>
                  <th>Job ID</th>
                  <th>Name</th>
                  <th>Version</th>
                  <th>Hosts</th>
                  <th>Command</th>
                  <th>Environment</th>
                </tr>
              </thead>
              <tbody>
                <#list jobs as job>
                <tr>
                  <td>${job.id?html}</td>
                  <td>${job.name}</td>
                  <td>${job.version}</td>
                  <td>${job.hosts}</td>
                  <td>${job.command?html}</td>
                <td>${job.env?html}</td>
                </tr>
                </#list>
              </tbody>
            </table>
          </div>

          <h2 class="sub-header">Hosts</h2>
          <div class="table-responsive">
            <table class="table table-striped">
              <thead>
                <tr>
                  <th>Host</th>
                  <th>Status</th>
                  <th>Deployed</th>
                  <th>Running</th>
                  <th>CPUs</th>
                  <th>Mem</th>
                  <th>Load Avg</th>
                  <th>Mem Usage</th>
                  <th>OS</th>
                </tr>
              </thead>
              <tbody>
                <#list hosts as host>
                <tr>
                  <td>${host.name?html}</td>
                  <td>${host.status?html}</td>
                  <td>${host.jobsDeployed}</td>
                  <td>${host.jobsRunning}</td>
                  <td>${host.cpus}</td>
                  <td>${host.mem}</td>
                  <td>${host.loadAvg}</td>
                  <td>${host.memUsage}</td>
                  <td>${host.osName} ${host.osVersion}</td>
                  <td>${host.heliosVersion}</td>
                  <td>${host.dockerVersion}</td>
                </tr>
                </#list>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="//code.jquery.com/jquery-2.1.1.min.js"></script>
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"></script>
  </body>
</html>