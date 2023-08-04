class A {

    init() { 
        this.firstName = "Kanishka";
        return this;    
    }

    cook() {
        print this.firstName + " " + this.lastName + " is cooking."; 
    }
}



class B < A {

    init() {
        super.init();
        this.lastName = "Tiwari";  
    }

    cook() {
        super.cook(); 
        print "Let him cook."; 
    }
}




