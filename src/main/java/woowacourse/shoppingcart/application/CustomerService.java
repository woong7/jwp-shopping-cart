package woowacourse.shoppingcart.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woowacourse.auth.application.AuthorizationException;
import woowacourse.shoppingcart.dao.CustomerDao;
import woowacourse.shoppingcart.domain.Customer;
import woowacourse.shoppingcart.dto.CustomerRequest;
import woowacourse.shoppingcart.dto.CustomerResponse;
import woowacourse.shoppingcart.exception.DuplicateNameException;
import woowacourse.shoppingcart.support.Encryptor;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomerService {
    private final CustomerDao customerDao;
    private final Encryptor encryptor;

    public CustomerService(final CustomerDao customerDao, final Encryptor encryptor) {
        this.customerDao = customerDao;
        this.encryptor = encryptor;
    }

    public void addCustomer(final CustomerRequest customerRequest) {
        if (customerDao.existsByName(customerRequest.getName())) {
            throw new DuplicateNameException();
        }
        String encryptedPassword = encryptor.encrypt(customerRequest.getPassword());
        customerDao.save(customerRequest.getName(), encryptedPassword);
    }

    public void deleteCustomerByName(final String customerName) {
        customerDao.deleteByName(customerName);
    }

    public CustomerResponse findCustomerByName(final String customerName) {
        final Customer customer = customerDao.findCustomerByName(customerName);
        return new CustomerResponse(customer.getName());
    }

    public void editCustomerByName(final String customerName, final CustomerRequest editRequest) {
        String encryptedPassword = encryptor.encrypt(editRequest.getPassword());
        customerDao.updateByName(customerName, encryptedPassword);
    }

    public void validateNameAndPassword(final String name, final String password) {
        String encryptedPassword = encryptor.encrypt(password);
        if (customerDao.existsIdByNameAndPassword(name, encryptedPassword)) {
            return;
        }
        throw new AuthorizationException("로그인에 실패했습니다.");
    }
}
