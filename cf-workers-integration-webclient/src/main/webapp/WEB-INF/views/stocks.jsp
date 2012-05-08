<!doctype html>
<html ng-app>
<head>

    <script src="${context}/web/assets/js/jquery-1.7.2.min.js"></script>
    <script src="${context}/web/assets/js/angular-1.0.0rc6.js"></script>
    <link rel="stylesheet" href="${context}/web/assets/bootstrap/bootstrap.css">
    <script src="${context}/web/views/stocks.js"></script>
	<link rel="stylesheet" href="${context}/web/views/stocks.css"/>
</head>
<body>
 <script language = "javascript" type = "text/javascript">
     <!--
		 $(function(){
		  utils.setup( '${context}'); 
		})
 	   //   utils.setup( '${context}');
    //-->
    </script>

<div ng-controller="StockCtrl">
    <div>
        <form class="well form-search" ng-submit="lookupStock()">
            <label> Search by ticker symbol</label>
            <input type="text" ng-model="ticker" class="input-medium search-query" width="5" size="5" placeholder="stock ticker symbol">
            <button type="submit" class="btn btn-primary" ng-click="lookupStock()" >
                <a class="icon-search"></a>
            </button>
        </form>
    </div>

    <!-- http://stocks-web.cloudfoundry.com/stocks/vmw
    {"id":718288,"exchange":"NYSE","changeWhileOpen":-3.34,"ticker":"VMW","highPrice":107.38,"lowPrice":104.26,"lastValueWhileOpen":104.26}
    -->

    <form class="form-horizontal" ng-submit="updateCustomer">
        <fieldset>
            <legend> Information on {{stock.ticker}}
            </legend>


    <div class="control-group">
        <span class="control-label" >Ticker:</span>
        <div class="controls">
            {{stock.ticker}}
        </div>
    </div>


     <div class="control-group">
         <span class="control-label" >ID:</span>
         <div class="controls">


                 {{stock.id}}

         </div>
     </div>


    <div class="control-group">
        <span class="control-label" >Low Price:</span>
        <div class="controls">


            {{stock.lowPrice}}
        </div>
    </div>

    <div class="control-group">
        <span class="control-label" >High Price:</span>
        <div class="controls">

            {{stock.highPrice}}
        </div>
    </div>



    <div class="control-group">
        <span class="control-label" >Change While Open:</span>
        <div class="controls">

            {{stock.changeWhileOpen}}
        </div>
    </div>

    <div class="control-group">
        <span class="control-label" >Exchange:</span>
        <div class="controls">



            {{stock.exchange}}

        </div>
    </div>
            </fieldset></form>


</div>
</body>
</html>