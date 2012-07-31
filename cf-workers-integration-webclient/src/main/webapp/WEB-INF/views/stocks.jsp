<!doctype html>
<html ng-app>
<head>
    <script src="${context}/web/assets/js/jquery-1.7.2.min.js"></script>
    <script src="${context}/web/assets/js/angular-1.0.0rc6.js"></script>
    <link rel="stylesheet" href="${context}/web/assets/bootstrap/bootstrap.css">
    <script src="${context}/web/views/stocks.js"></script>
    <link rel="stylesheet" href="${context}/web/views/stocks.css"/>
    <script language="javascript" type="text/javascript">
        $(function () {
            utils.setup('${context}');
        });
    </script>
</head>
<body>

<div style="margin: 20px;" ng-controller="StockCtrl">
    <div>
        <form class="well form-search" ng-submit="lookupStock()"><label> Search by ticker symbol</label>
            <input type="text" ng-model="ticker" class="input-medium search-query" width="5" size="5" placeholder="stock ticker symbol">
            <button type="submit" class="btn btn-primary" ng-click="lookupStock()"><a class="icon-search"></a></button>
        </form>
    </div>
    <form class="form-horizontal" ng-submit="updateCustomer">
    
        <fieldset>
            <legend> Information on {{stock.ticker}}</legend>
            <div class="control-group"><span class="control-label">Ticker:</span>
                <div class="controls"> {{stock.ticker}}</div>
            </div>
            <div class="control-group"><span class="control-label">Last Value While Open:</span>
                <div class="controls"> {{stock.lastValueWhileOpen}}</div>
            </div>             
            <div class="control-group"><span class="control-label">Exchange:</span>
                <div class="controls"> {{stock.exchange}}</div>
            </div>
        </fieldset>
    </form>
</div>
</body>
</html>