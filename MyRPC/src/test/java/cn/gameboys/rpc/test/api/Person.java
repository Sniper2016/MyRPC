package cn.gameboys.rpc.test.api;

public class Person {

	private int userID;
	private String firstName;
	private String lastName;

	public Person() {
	}

	public Person(int userID,String firstName, String lastName) {
		this.userID = userID;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	
	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	public String toString() {
		return firstName + " " + lastName;
	}

	@Override
	public int hashCode() {
		return this.firstName.hashCode() ^ this.lastName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Person))
			return false;
		Person p = (Person) obj;
		return this.firstName.equals(p.firstName) && this.lastName.equals(p.lastName);
	}
}
