// test
class Simple {
	public static void main (String[] s) {
		/* Completely 
			empty */
	}
}

class Cat extends Animal {
	int legs;
	public int AddAndReturnLegs(int leg) {
		int ret;
		Animal a;
		long ret1;
		if (legs + leg <= 100) {
			ret = legs + leg;
		} else 
			ret = legs + leg - 100;
		a = (new Animal()).main();
		a = (new Cat()).main();
		a = a.NumberOfLegs();
		a = (new Cat()).NumberOfLegs();
		ret1 = ret*32l;
		ret1 = ret1*2;
		if (ret1 > 100) 
			ret1 = 2;
		if (ret == ret1) 
			return ret;
		return ret;
	}
	public Cat NumberOfLegs() {
		int leg;
		Cat cat;
		cat = new Cat();
		leg = 23 + 4 * 5;
		leg = leg + leg - 4 * leg + (leg-1)*leg;
		leg = leg - (1 + 5);
		leg = leg - (1 - 5);
		leg = cat.AddAndReturnLegs(leg);
		return cat;
	}
}

class Animal {
	public Cat main() {
		return new Cat();
	}
	public Animal NumberOfLegs() {
		return this.main();
	}
}