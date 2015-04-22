/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: SunGard CSA LLC - initial API and implementation and/or initial
 * documentation
 ******************************************************************************/

/**
 * @author Johnson.Quadras
 */

(function() {
    'use strict';
    angular.module('admin-ui').controller(
	    'sdPasswordManagementCtrl',
	    [ '$scope', '$q', '$filter', 'sdDialogService', 'sgI18nService', 'sdLoggerService',
		    'sdPasswordManagementService', Controller ]);

    var _trace = null;
    var _sdPasswordManagementService = null;
    var _sdDialogService = null;
    var _sgI18nService = null;
    var invalidNumberMesssage = null;
    var rangeErrorMessage = null;
    var interpolate = null;

    var DEFAULTS = {
	OPTIONS : {
	    PASWORD_STRENGTH : {
		MINIMUM_PASSWORD_LENGTH : [],
		REQUIRED_NUMBER_LETTERS : [],
		REQUIRED_MIXED_CASE_LETTERS : [],
		REQUIRED_NUMBER_DIGITS : [],
		REQUIRED_SYMBOLS : []
	    },
	    PASWORD_REUSE : {
		PREVIOUS_PASSWORDS_TO_CHECK : [],
		MINIMUM_CHARACTER_DIFFERENCE : []
	    },
	    PASSWORD_EXPIRATION_POLICY : {
		EXPIRATION_TIME_MIN : 1,
		EXPIRATION_TIME_MAX : 999,
		SEND_NOTIFICATION_MAILS_BLANK : 1,
		SEND_NOTIFICATION_MAILS_MIN : 0,
		SEND_NOTIFICATION_MAILS_MAX : 999,
		DISABLE_ACCOUNT_MIN : -1,
		DISABLE_ACCOUNT_MAX : 999
	    }
	},
	PASSOWORD_RULES : {
	    isStrongPassword : false,
	    isForcePasswordChange : false,
	    isUniquePassword : false,
	    minimalPasswordLength : 6,
	    passwordTracking : 0,
	    differentCharacters : 0,
	    digits : 0,
	    letters : 0,
	    mixedCase : 0,
	    symbols : 0,
	    expirationTime : 90,
	    disableUserTime : -1,
	    notificationMails : 3,
	    passwordEncrption : false
	}
    };

    var NUMBER_ONLY_EXP = /^\d+$/;

    /*
     * 
     */
    function Controller($scope, $q, $filter, sdDialogService, sgI18nService, sdLoggerService,
	    sdPasswordManagementService) {
	var self = this;
	_sdPasswordManagementService = sdPasswordManagementService;
	_trace = sdLoggerService.getLogger('admin-ui.sdPasswordManagementCtrl');
	_sdDialogService = sdDialogService;
	_sgI18nService = sgI18nService;

	invalidNumberMesssage = _sgI18nService.translate('html5-common.converter-number-error', 'Invalid Number');
	interpolate = $filter('interpolate');
	rangeErrorMessage = _sgI18nService.translate('html5-common.range-error', "Specified value is not in range");

	self.intialize();

	/*
	 * 
	 */
	self.setFormValidity = function(validity) {
	    $scope.passwordMgmtForm.$setValidity("Invalid values", validity);
	};
	/**
	 * 
	 */
	self.savePasswordRules = function() {

	    if (!$scope.passwordMgmtForm.$invalid && !$scope.passwordMgmtForm.$pristine) {
		self.showConfirmationDialog($scope);
	    } else {
		_trace.debug("Not saving password rules.Reason - Form valid:" + !$scope.passwordMgmtForm.$invalid
			+ " ,Form unchanged :" + $scope.passwordMgmtForm.$pristine);
	    }

	};

	/**
	 * 
	 */
	self.showSuccessNotification = function() {
	    self.showSuccessNotificationDialog($scope);
	};

	/**
	 * 
	 */
	self.updatePasswordRules = function() {
	    var deferred = $q.defer();
	    _trace.debug("Updating password rules", self.passwordRules);
	    _sdPasswordManagementService.savePasswordRules(self.passwordRules).then(function(result) {
		deferred.resolve("Update successfull");
		$scope.passwordMgmtForm.$setPristine(true);
		self.showSuccessNotification()
		_trace.debug("Password rules updated successfully");
	    }, function(error) {
		deferred.resolve("Update successfull");
		_trace.error("Failed to updated Password rules.Response : ", error);
	    });
	    return deferred.$promise;
	};
    }

    /**
     * 
     */
    Controller.prototype.intialize = function() {
	var self = this;
	self.passwordRules = {};
	self.passwordEncryptionFieldDisabled = true;
	self.populateOptions();
	self.getPasswordRules();
    };

    /**
     * 
     */
    Controller.prototype.getPasswordRules = function() {
	var self = this;
	self.passwordRules = {};
	_trace.debug("Retreiving password rules.");
	_sdPasswordManagementService.getPasswordRules().then(function(result) {
	    if (result.rules && result.rules.rulesAvailable) {
		self.passwordRules = result.rules;
		_trace.debug("Password rules available. Rules : ", self.passwordRules)
	    } else {
		self.passwordRules = DEFAULTS.PASSOWORD_RULES;
		_trace.debug("Password rules not available. Default Rules set : ", self.passwordRules)
	    }
	    self.calculateMinCharDifference();
	}, function(error) {
	    _trace.error("Failed to retrieve password rules.Error : ", error);
	});
    };

    /**
     * 
     */
    Controller.prototype.calculateMinCharDifference = function() {
	var self = this;
	self.calulatePasswordLength();
	self.minCharacterDifferenceOptions = getArrayWithNumber(0, self.passwordRules.minimalPasswordLength);
	if (self.passwordRules.differentCharacters > self.passwordRules.minimalPasswordLength) {
	    self.passwordRules.differentCharacters = DEFAULTS.PASSOWORD_RULES.differentCharacters;
	}
    };

    /**
     * 
     */
    Controller.prototype.calulatePasswordLength = function() {
	var self = this;
	var cntLetters = self.passwordRules.mixedCase * 2;
	if (self.passwordRules.letters > cntLetters) {
	    cntLetters = self.passwordRules.letters;
	}

	var cnt = cntLetters + self.passwordRules.digits + self.passwordRules.symbols;
	if (cnt > self.passwordRules.minimalPasswordLength) {
	    self.passwordRules.minimalPasswordLength = cnt;
	    return;
	}

	return self.passwordRules.minimalPasswordLength;
    }

    /**
     * 
     */
    Controller.prototype.populateOptions = function() {
	var self = this;

	self.minPasswordLengthOptions = getArrayWithNumber(4, 32);
	self.numberOfLettersOptions = getArrayWithNumber(0, 6);
	self.mixedCaseLettersOptions = getArrayWithNumber(0, 6);
	self.numberOfDigitsOptions = getArrayWithNumber(0, 6);
	self.symbolsOptions = getArrayWithNumber(0, 6);
	self.previousPasswordsToCheckOptions = getArrayWithNumber(0, 10);
    };

    /**
     * 
     */
    Controller.prototype.getDefaultOnBlank = function(field) {
	var self = this;
	if (field === 'MAX_PASSWORD_AGE' && self.passwordRules.expirationTime === '') {
	    self.passwordRules.expirationTime = DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.MAX_PASSWORD_AGE_BLANK;
	}

	if (field === 'NOTIFICATION_EMAIL_DAYS' && self.passwordRules.notificationMails === '') {
	    self.passwordRules.notificationMails = DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.NOTIFICATION_EMAIL_DAYS_BLANK;
	}

	if (field === 'DISABLE_ACCOUNT_DAYS_AFTER_EXPIRATION' && self.passwordRules.disableUserTime === '') {
	    self.passwordRules.disableUserTime = DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.DISABLE_ACCOUNT_DAYS_AFTER_EXPIRATION;
	}
    };

    /**
     * 
     */
    Controller.prototype.validateExpiryTime = function() {
	var self = this;
	var value = self.passwordRules.expirationTime;
	self.expirationTimeError = false;
	self.expirationTimeErrorMsg = "";
	if (self.passwordRules.expirationTime == '') {
	    self.passwordRules.expirationTime = DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.EXPIRATION_TIME_MIN;
	    return;
	}
	if (DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.EXPIRATION_TIME_MIN > value
		|| DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.EXPIRATION_TIME_MAX < value) {
	    self.expirationTimeError = true;
	    self.expirationTimeErrorMsg = interpolate(rangeErrorMessage, [
		    DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.EXPIRATION_TIME_MIN,
		    DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.EXPIRATION_TIME_MAX ]);
	} else if (!NUMBER_ONLY_EXP.test(value)) {
	    self.expirationTimeError = true;
	    self.expirationTimeErrorMsg = invalidNumberMesssage;
	}

	self.setFormValidity(!self.expirationTimeError);
    };

    /**
     * 
     */
    Controller.prototype.validateNotificationMails = function() {
	var self = this;
	var value = self.passwordRules.notificationMails;
	self.notificationMailsError = false;
	self.notificationMailsErrorMsg = "";
	if (self.passwordRules.notificationMails == '') {
	    self.passwordRules.notificationMails = DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.SEND_NOTIFICATION_MAILS_BLANK;
	    return;
	}

	if (DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.SEND_NOTIFICATION_MAILS_MIN > value
		|| DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.SEND_NOTIFICATION_MAILS_MAX < value) {
	    self.notificationMailsError = true;
	    self.notificationMailsErrorMsg = interpolate(rangeErrorMessage, [
		    DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.SEND_NOTIFICATION_MAILS_MIN,
		    DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.SEND_NOTIFICATION_MAILS_MAX ]);
	} else if (!NUMBER_ONLY_EXP.test(value)) {
	    self.notificationMailsError = true;
	    self.notificationMailsErrorMsg = invalidNumberMesssage;
	}

	self.setFormValidity(!self.notificationMailsError);
    };

    /**
     * 
     */
    Controller.prototype.validateDisableAccountDays = function() {
	var self = this;
	var value = self.passwordRules.disableUserTime;
	self.disableUserTimeError = false;
	self.disableUserTimeErrorMsg = "";
	if (self.passwordRules.disableUserTime == '') {
	    self.passwordRules.disableUserTime = DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.DISABLE_ACCOUNT_MIN;
	    return;
	}

	if (DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.DISABLE_ACCOUNT_MIN > value
		|| DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.DISABLE_ACCOUNT_MAX < value) {
	    self.disableUserTimeError = true;
	    self.disableUserTimeErrorMsg = interpolate(rangeErrorMessage, [
		    DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.DISABLE_ACCOUNT_MIN,
		    DEFAULTS.OPTIONS.PASSWORD_EXPIRATION_POLICY.DISABLE_ACCOUNT_MAX ]);
	} else if (!NUMBER_ONLY_EXP.test(value)) {
	    self.disableUserTimeError = true;
	    self.disableUserTimeErrorMsg = invalidNumberMesssage;
	}

	self.setFormValidity(!self.disableUserTimeError);
    };

    /**
     * 
     */
    Controller.prototype.showConfirmationDialog = function($scope) {
	var self = this;

	var title = _sgI18nService.translate('views-common-messages.common-confirm', 'Confirm');
	var message = _sgI18nService.translate('admin-portal-messages.views-passwordMgmt-saveConfirmation',
		'Are you sure you want to save the Password Security settings?');

	if (self.passwordRules.passwordEncrption) {
	    title = _sgI18nService.translate('views-common-messages.common-encryptConfirmation',
		    'Encrypt Password Confirmation');
	    message = _sgI18nService.translate('admin-portal-messages.views-passwordMgmt-passwordEncryption',
		    'Are you sure you want to enable password encryption? This operation cannot be undone.');
	}

	var html = "<span>" + message + " </span>";
	var confirmActionLabel = _sgI18nService.translate('views-common-messages.common-Yes', 'Yes');
	var cancelActionLabel = _sgI18nService.translate('views-common-messages.common-No', 'No');

	var options = {
	    title : title,
	    confirmActionLabel : confirmActionLabel,
	    cancelActionLabel : cancelActionLabel,
	    onConfirm : self.updatePasswordRules,
	    type : 'confirm'
	}
	_sdDialogService.dialog($scope, options, html);

    };

    /**
     * 
     */
    Controller.prototype.showSuccessNotificationDialog = function($scope) {

	var title = _sgI18nService.translate('portal-common-messages.common-info', 'Information');
	var message = _sgI18nService.translate('admin-portal-messages.views-passwordMgmt-saveSuccess',
		'Save Sucessfull');
	_sdDialogService.info($scope, message, title)
    };

    /**
     * 
     */
    function getArrayWithNumber(from, to) {
	var array = [];
	for (var i = from; i <= to; i++) {
	    array.push(i);
	}
	return array;
    }

})();