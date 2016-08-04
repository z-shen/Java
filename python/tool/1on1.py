service = []
access = []

with open('ad4769_2uniqService','rb') as f:
    for service1 in f:
        service.append(service1)
with open('ad4769_2accountAccessService','rb') as f:
    for accessService in f:
        access.append(accessService)

for item in service:
#    print '%s' % item
    for access1 in access:
#        print '%s' % access1.split('\t')[0]
        if item.strip() in access1.strip().split('\t')[1]:
            print '%s' % access1
        

