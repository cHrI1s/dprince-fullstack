export default class LocalDatabase {
    constructor() {
        this.databaseName = "local__database";
        this.version = 1;
        this.stores = ["blobs"];

        this.database = null;
        this.initializeDb().then(database=>this.database = database);
    }

    createStores(database){
        this.stores.forEach(singleStore=>{
            if (!database.objectStoreNames.contains(singleStore)) {
                database.createObjectStore(singleStore, {keyPath: 'id'}); // create it
            }
        });
        return database;
    }
    initializeDb(){
        return new Promise(resolve=>{
            let openRequest = indexedDB.open(this.databaseName, this.version);
            openRequest.onupgradeneeded = ()=>{
                let database = openRequest.result;
                database = this.createStores(database);
                resolve(database);
            };

            openRequest.onsuccess = ()=> {
                let database = openRequest.result;
                database = this.createStores(database);
                resolve(database);
            };

            openRequest.onerror = ()=> {
                resolve(null);
            };
        });
    }

    delete(){
        let request = indexedDB.deleteDatabase(this.databaseName);
    }
    clean(){
        this.initializeDb().then(database=>{
            this.stores.forEach(singleStore=>{
                if(typeof database.objectStore!=='undefined') {
                    let request = database.objectStore(singleStore);
                    request.clear();
                }
            });
        });
    }


    store(store, key, value){
        return new Promise(resolve => {
            this.initializeDb().then(database=>{
                if(database!==null){
                    let transaction = database.transaction(store, "readwrite");

                    let theStore = transaction.objectStore(store);
                    let theObject = {
                        id: key,
                        value: value
                    };
                    let request = theStore.add(theObject); // (3)
                    request.onsuccess = ()=> {
                        resolve(true);
                    };

                    request.onerror = ()=> {
                        resolve(false);
                    };
                }
            });
        });
    }

    update(store, key, value){
        return new Promise(resolve => {
            this.initializeDb().then(database=>{
                if(database!==null){
                    let transaction = database.transaction(store, "readwrite");

                    let theStore = transaction.objectStore(store);
                    if(typeof key!=='undefined') {
                        let request = theStore.delete(key);
                        request.onerror = () => {
                            this.store(store, key, value).then(result => {
                                resolve(result);
                            });
                        }
                        request.onsuccess = () => {
                            this.store(store, key, value).then(result => {
                                resolve(result);
                            });
                        }
                    } else {
                        resolve(false);
                    }
                }
            });
        });
    }

    get(store, key){
        return new Promise(resolve => {
            this.initializeDb().then(database=>{
                if(database!==null){
                    let transaction = database.transaction(store, "readonly");

                    let theStore = transaction.objectStore(store);
                    let request = theStore.get(key);
                    request.onerror = ()=> {
                        resolve(null);
                    }
                    request.onsuccess = ()=> {
                        if (request.result !== undefined) resolve(request.result);
                        else resolve(null);
                    }
                }
            });
        });
    }
    deleteFromDb(store, key, value){
        return new Promise(resolve => {
            this.initializeDb().then(database=>{
                if(database!==null){
                    let transaction = database.transaction(store, "readwrite");

                    let theStore = transaction.objectStore(store);
                    let theObject = {
                        id: key,
                        value: value
                    };
                    let request = theStore.delete(theObject); // (3)
                    request.onsuccess = function() {
                        resolve(true);
                    };

                    request.onerror = function() {
                        resolve(false);
                    };
                }
            });
        });
    }
}