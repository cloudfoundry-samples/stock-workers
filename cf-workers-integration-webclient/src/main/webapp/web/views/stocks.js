/***
 * Controller to handle interfacing with the RESTful endpoint
 */
$.ajaxSetup({
    cache:false
});

var utils = {
    _url:'',
    setup:function (u) {
        this._url = u;
    },
    url:function (u) {
       return this._url + u;
    },
    get:function (url, data, cb) {
        $.ajax({
            type:'GET',
            url:url,
            cache:false,
            dataType:'json',
            contentType:'application/json; charset=utf-8',
            success:cb,
            error:function () {
                alert('error trying to retrieve ' + u);
            }
        });
    }
};

/**
 * Data comes from http://stocks-web.cloudfoundry.com/stocks/${STOCK TICKER SYMBOL} where ${STOCK TICKER SYMBOL} might be, for example, "VMW."
 *
 * A sample result is: {"id":718288,"exchange":"NYSE","changeWhileOpen":-3.34,"ticker":"VMW","highPrice":107.38,"lowPrice":104.26,"lastValueWhileOpen":104.26}
 *
 */
function StockCtrl($scope) {

    $scope.ticker = 'VMW';
    $scope.stock = null;

    function loadStockByTicker(ticker, cb) {
        var u = utils.url('/stocks/' + ticker);
        utils.get( u, {}, cb);
    }

    $scope.lookupStock = function () {
        loadStockByTicker($scope.ticker, function (c) {
            $scope.$apply(function () {
                $scope.stock = c;
            });
        });
    };

    $scope.lookupStock(); // when the page loads, it should lookup the default stock

}

