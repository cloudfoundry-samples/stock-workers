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

}

