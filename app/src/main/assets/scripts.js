angular.module('Recog', [])
.controller('RecogCtrl', ['$scope', function($scope) {
  $scope.text = '1.Click mic  2.Allow access  3.Start speaking';

  var recognition = new webkitSpeechRecognition();
  recognition.lang = 'fa-IR';
  recognition.continuous = true;
  recognition.interimResults = true;

  recognition.onresult = function(event) {
    for(var i = event.resultIndex; i < event.results.length; ++i) {
      $scope.$apply(function() {
        $scope.text = event.results[i][0].transcript;
      });
      if(event.results[i].isFinal) {
        $scope.$apply(function() {
          $scope.text = event.results[i][0].transcript;
        });
      }
    }
  };

  $scope.on = false;

  $scope.startStop = function() {
    $scope.on = !$scope.on;
    if($scope.on) {
      recognition.start();
    } else {
      recognition.stop();
    }
  };
}]);
