const options = {
  method: 'POST',
  headers: {
    accept: 'application/json',
    'content-type': 'application/json',
    access_token: {ACCESS_TOKEN}
  },
  body: JSON.stringify({
    name: getNome,
    cpfCnpj: getCpfCnpj,
    email: getEmail,
    phone: getPhone,
    mobilePhone: getMobile,
    address: getAdress,
    addressNumber: getAddressNumber,
    complement: getComplement,
    province: getProvince,
    postalCode: getPostalCode,
    externalReference: getExternalReference,
    notificationDisabled: false,
    additionalEmails: getAdditionalEmails,
    municipalInscription: getMunicipalInscription,
    stateInscription: getStateInscription,
    observations: getObeservations,
    groupName: null,
    company: null,
    foreignCustomer: getIdCustomer
  })
};

fetch('https://api-sandbox.asaas.com/v3/customers', options)
  .then(res => res.json())
  .then(res => console.log(res))
  .catch(err => console.error(err));