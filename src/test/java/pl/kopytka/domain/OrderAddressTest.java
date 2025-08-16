package pl.kopytka.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderAddressTest {

    @Test
    void shouldCreateValidAddress() {
        //given
        var street = "Marianowa";
        var postCode = "888-88";
        var city = "Paździochowo";
        var houseNo = "11";

        //when
        var address = new OrderAddress(street, postCode, city, houseNo);

        //then
        assertEquals(street, address.getStreet());
        assertEquals(postCode, address.getPostCode());
        assertEquals(city, address.getCity());
        assertEquals(houseNo, address.getHouseNo());
    }
}
