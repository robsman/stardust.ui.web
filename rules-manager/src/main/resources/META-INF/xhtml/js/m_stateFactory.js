/*Simple state object to encapsulate three basic conditions of a piece of data...
 * 1. isPersisted: is it resident in some format in a persistent state? (file,database,etc...)
 * 2. isDirty: has the data been modified at all since arbitrary event or time?
 * 3. isDeleted: has this piece of data been marked for deletion?
 * 
 * Example decision table based on states
 * ----------------------------------------------
 * isPersisted isDirty isDeleted Action
 * ----------------------------------------------
 *  T			T		T		Delete From Archive
 *	F			T		T		No Action
 *	T			F		T		Delete From Archive
 *	T			T		F		Update in Archive
 *	T			F		F		No Action
 *	F			F		T		No Action
 *	F			T		F		Save in Archive
 *	F			F		F		Save in Archive
 * */
define([],function(){
	return{
		create: function(persisted,dirty,deleted){
			return {
				isPersisted: persisted || false,
				isDirty: dirty || false,
				isDeleted: deleted || false
			};
		}
	};
});